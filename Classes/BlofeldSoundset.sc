BlofeldSoundset {
	const <allSoundsBank = 0x40;

	classvar <loaded;
	classvar <files;

	var <blofeld;
	var <sounds;
	var <>name;

	*initClass {
		var sep = thisProcess.platform.pathSeparator;
		var filesDir = this.filenameSymbol.asString.dirname.dirname ++ sep ++ "Soundsets";
		files = ();
		files.put(\factory2012, (filesDir ++ sep ++ "factory_set_2012.mid"));
		files.put(\factory2008, (filesDir ++ sep ++ "blofeld_fact_080223.syx"));
		files.put(\easterset, (filesDir ++ sep ++ "blo_easterset.mid"));
		files.put(\Lanthans, (filesDir ++ sep ++ "LanthansSoundset.syx"));
		files.put(\ForumOneTwoThree, (filesDir ++ sep ++ "microQ" ++ sep ++ "ForumOneTwoThree_Blofeld.mid"));
		files.put(\FlakScrambler, (filesDir ++ sep ++ "microQ" ++ sep ++ "FlakScrambler_Blofeld.mid"));
		files.put(\DocT, (filesDir ++ sep ++ "microQ" ++ sep ++ "DocT_Blofeld.mid"));
		files.put(\uQ2001, (filesDir ++ sep ++ "microQ" ++ sep ++ "uQ2001_Blofeld.mid"));
		loaded = ();
	}

	*loadAll {
		files.keys.do { |key|
			loaded.put(key, this.load(files[key]));
		};
	}

	*load { |path|
		var obj = this.new;
		if (path.extension.toLower == "mid", {
			var midiFile = SimpleMIDIFile.read(path);
			midiFile.sysexEvents.do { |sysexEvent|
				// 0 track
				// 1 absTime
				// 2 WaldorfID
				// 3 BlofeldID
				// 4 DeviceID
				// 5 SoundDump
				// 6 Bank
				// 7 Program
				// 8-390 Sound Data 0
				// 391 Checksum
				// 392 -9
				var data = sysexEvent[8..390];
				var validChksum = (sysexEvent[391] == 0x7F) || (sysexEvent[391] == BlofeldSysex.checksum(data));
				if (sysexEvent[2] == BlofeldSysex.waldorfID && sysexEvent[3] == BlofeldSysex.blofeldID && validChksum, {
					obj.add(BlofeldSound.new(sysexEvent[6], sysexEvent[7], data));
				});
			};
		}, {
			var sysexFile = SysexFile.read(path);
			sysexFile.events.do { |sysexEvent|
				// 0 WaldorfID
				// 1 BlofeldID
				// 2 DeviceID
				// 3 SoundDump
				// 4 Bank
				// 5 Program
				// 6-388 Sound Data 0
				// 389 Checksum
				var data = sysexEvent[6..388];
				var validChksum = (sysexEvent[389] == 0x7F) || (sysexEvent[389] == BlofeldSysex.checksum(data));
				if (sysexEvent[0] == BlofeldSysex.waldorfID && sysexEvent[1] == BlofeldSysex.blofeldID && validChksum, {
					obj.add(BlofeldSound.new(sysexEvent[4], sysexEvent[5], data));
				});
			};
		});
		obj.name = files.findKeyForValue(path) ?? path.basename.asSymbol;
		^obj;
	}

	*select { |function|
		var selected = [];
		loaded.do { |soundset|
			selected = selected ++ soundset.sounds.select { |sound|
				function.value(sound, soundset);
			};
		};
		^selected;
	}

	*new { |blofeld = nil, sounds = nil|
		if (sounds.isCollection.not, {
			sounds = ();
		});
		^super.newCopyArgs(blofeld, sounds);
	}

	getOrCreate { |bank, program|
		var key = Blofeld.key(bank, program);
		var sound = sounds[key];
		if (sound == nil, {
			sound = BlofeldSound.new(bank, program);
			this.add(sound);
		});
		^sound;
	}

	get { |bank, program|
		^sounds[Blofeld.key(bank, program)];
	}

	choose { |category = nil|
		var set = sounds;
		if (category != nil, {
			set = set.select({|s| s.get(\category) == category })
		});
		^set.choose;
	}

	add { |sound|
		sounds.put(sound.key, sound);
	}

	remove { |bank, program|
		var key = Blofeld.key(bank, program);
		sounds.removeAt(key);
	}

	validate {
		if (sounds.size > 1024, {
			Error("Maximum 1024 sounds per sound set").throw;
		});
	}

	saveToFile { |path, deviceID = 0x00|
		var sorted;
		this.validate;
		sorted = sounds.asList.sort({ |a, b|
			if (a.bank == b.bank, {
				a.program < b.progam
			}, {
				a.bank < b.bank
			});
		});
		if (path.extension.toLower == "mid", {
			var midiFile = SimpleMIDIFile.new(path);
			sorted.do { |sound|
				var packet = BlofeldSysex.soundDumpPacket(sound, deviceID, false);
				// prefix with [track, absTime]
				midiFile.addSysexEvent([0, 0] ++ packet);
			};
			midiFile.write;
		}, {
			var sysexFile = SysexFile.new(path);
			sorted.do { |sound|
				var packet = BlofeldSysex.soundDumpPacket(sound, deviceID, false);
				sysexFile.addEvent(packet);
			};
			sysexFile.write;
		});
	}

	download { |sound, callback = nil|
		BlofeldSysex.soundDumpCallback.put(sound.key, this.expect(callback));
		blofeld.midiOut.sysex(BlofeldSysex.soundRequestPacket(sound.bank, sound.program, blofeld.deviceID));
	}

	downloadAll { |callback = nil|
		var r = Routine({
			Blofeld.bank.values.sort.do { |b|
				128.do { |i|
					BlofeldSysex.soundDumpCallback.put(Blofeld.key(b, i), this.expect(callback));
				}
			};
			blofeld.midiOut.sysex(BlofeldSysex.soundRequestPacket(allSoundsBank, 0x00, blofeld.deviceID));
			(Blofeld.bank.size*128).wait;
			if (callback != nil, { callback.value });
		});
		r.play;
		^r;
	}

	upload { |sound, callback = nil|
		var r = Routine({
			blofeld.midiOut.sysex(BlofeldSysex.soundDumpPacket(sound, blofeld.deviceID));
			this.add(sound);
			if (callback != nil, {
				1.wait;
				callback.value;
			});
		});
		r.play;
		^r;
	}

	uploadAll { |callback = nil|
		var r = Routine({
			Blofeld.bank.values.sort.do { |b|
				128.do { |i|
					var sound = get(b, i);
					if (sound != nil, {
						this.upload(sound);
						1.wait;
					});
				};
			};
			if (callback != nil, { callback.value });
		});
		r.play;
		^r;
	}

	expect { |callback = nil|
		^{|bank, program, data|
			var sound = this.getOrCreate(bank, program);
			sound.data = data;
			if (callback != nil, { callback.value(sound); });
			BlofeldSysex.soundDumpCallback.removeAt(sound.key);
		}
	}
}
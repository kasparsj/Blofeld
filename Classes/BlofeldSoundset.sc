BlofeldSoundset {
	classvar <loaded;
	classvar <files;

	var <blofeld;
	var <sounds;
	var <>name;

	*initClass {
		var sep = thisProcess.platform.pathSeparator;
		var filesDir = PathName(this.filenameSymbol.asString.dirname.dirname ++ sep ++ "Soundsets");
		files = ();
		loaded = ();
		this.scanMidiFiles(filesDir, 1);
	}

	*scanMidiFiles { |folder, depth = 0|
		folder.files.do { |file|
			if ([\mid, \syx].indexOf(file.extension.toLower.asSymbol) != nil) {
				files.put(file.fileNameWithoutExtension.asSymbol, file.asAbsolutePath);
			};
		};
		if (depth > 0) {
			folder.folders.do { |subFolder|
				this.scanMidiFiles(subFolder, depth - 1);
			};
		};
	}

	*loadAll { |reload = false|
		files.keys.do { |key|
			this.load(files[key], reload);
		};
	}

	*load { |path, reload = false|
		var name, obj;
		path = if (files[path] != nil, { files[path] }, { path });
		name = PathName(path).fileNameWithoutExtension.asSymbol;
		obj = loaded[name];
		if ((obj == nil) || reload) {
			obj = this.new;
			if (PathName(path).extension.toLower == "mid") {
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
			} {
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
			};
			obj.name = name;
			loaded.put(obj.name, obj);
		};
		^obj;
	}

	*select { |function|
		var selected = [];
		loaded.do { |soundset|
			selected = selected ++ soundset.sounds.select { |sound|
				function.value(sound);
			};
		};
		^selected;
	}

	*new { |blofeld = nil, sounds = nil, name = nil|
		if (sounds.isCollection.not, {
			sounds = ();
		});
		^super.newCopyArgs(blofeld, sounds, name);
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

	getByName { |name|
		^sounds.detect({ |sound|
			sound.getName() == name;
		});
	}

	choose { |category = nil|
		var set = sounds;
		if (category != nil, {
			set = set.select({|s| s[\category] == category })
		});
		^set.choose;
	}

	add { |sound|
		sounds.put(Blofeld.key(sound.bank, sound.program), sound);
		sound.soundset = this;
	}

	remove { |bank, program|
		var key = Blofeld.key(bank, program);
		sounds[key].soundset = nil;
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
		if (PathName(path).extension.toLower == "mid", {
			var midiFile = SimpleMIDIFile.new(path);
			sorted.do { |sound|
				var packet = BlofeldSysex.soundDumpPacket(sound, deviceID);
				// prefix with [track, absTime]
				midiFile.addSysexEvent([0, 0] ++ packet);
			};
			midiFile.write;
		}, {
			var sysexFile = SysexFile.new(path);
			sorted.do { |sound|
				var packet = BlofeldSysex.soundDumpPacket(sound, deviceID);
				sysexFile.addEvent(packet);
			};
			sysexFile.write;
		});
	}

	download { |sound, callback = nil|
		blofeld.soundRequest(sound.bank, sound.program, this.onSoundDump(callback));
	}

	downloadAll { |callback = nil|
		var r = Routine({
			blofeld.soundRequest(Blofeld.allSoundsBank, 0x00, this.onSoundDump);
			(Blofeld.bank.size*128).wait;
			if (callback != nil, { callback.value(this) });
		});
		r.play;
		^r;
	}

	upload { |sound, callback = nil|
		var r = Routine({
			blofeld.soundDump(sound);
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
			if (callback != nil, { callback.value(this) });
		});
		r.play;
		^r;
	}

	onSoundDump { |callback = nil|
		^{|bank, program, data|
			var sound = this.getOrCreate(bank, program);
			sound.data = data;
			if (callback != nil, { callback.value(sound); });
		}
	}
}

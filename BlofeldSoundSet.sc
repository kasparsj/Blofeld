BlofeldSoundSet {
	const <allSoundsBank = 0x40;

	classvar <factorySet2012 = "factory_set_2012.mid";

	var <blofeld;
	var <sounds;

	*initClass {
		factorySet2012 = (this.filenameSymbol.asString.dirname ++ thisProcess.platform.pathSeparator ++ "factory_set_2012.mid");
	}

	*new { |blofeld = nil, sounds = nil|
		if (sounds.isCollection.not, {
			sounds = ();
		});
		^super.newCopyArgs(blofeld, sounds);
	}

	*newFromFile { |path|
		var obj = this.new;
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
			if (sysexEvent[2] == BlofeldSysex.waldorfID && sysexEvent[3] == BlofeldSysex.blofeldID && sysexEvent[391] == BlofeldSysex.checksum(data), {
				obj.add(BlofeldSound.new(sysexEvent[6], sysexEvent[7], data));
			});
		};
		^obj;
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
		var midiFile = SimpleMIDIFile.new(path);
		this.validate;
		// todo: sort sounds?
		sounds.do { |sound|
			var packet = BlofeldSysex.soundDumpPacket(sound, deviceID, false);
			// prefix with [track, absTime]
			midiFile.addSysexEvent([0, 0] ++ packet);
		};
		midiFile.write;
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
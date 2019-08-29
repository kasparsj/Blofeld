BlofeldWavetable {
	classvar <loaded;
	classvar <files;

	var <>slot;
	var <signal;
	var <>displayName;
	var <>name;
	var <>blofeld;

	*initClass {
		var sep = thisProcess.platform.pathSeparator;
		var filesDir = (this.filenameSymbol.asString.dirname.dirname ++ sep ++ "Wavetables").asPathName;
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

	*load { |path, reload = false, checkChecksum = true|
		var name = path.asPathName.fileNameWithoutExtension.asSymbol;
		var obj = loaded[name];
		if ((obj == nil) || reload) {
			var slot, displayName;
			obj = this.new;
			if (path.extension.toLower == "mid") {
				var midiFile = SimpleMIDIFile.read(path);
				midiFile.sysexEvents.do { |sysexEvent|
					// 0 track
					// 1 absTime
					// 2 WaldorfID
					// 3 BlofeldID
					// 4 DeviceID
					// 5 wavetableDump
					// 6 Slot
					// 7 Wave number
					// 8 Format
					// 9-392 Wave data
					// 393-406 Display name
					// 407 0x00
					// 408 0x00
					// 409 Checksum
					// 410 -9
					var data = sysexEvent[9..392];
					var validChksum = (sysexEvent[409] == 0x7F) || (sysexEvent[409] == BlofeldSysex.checksum(data));
					if ((sysexEvent[2] == BlofeldSysex.waldorfID) && (sysexEvent[3] == BlofeldSysex.blofeldID) && (validChksum || checkChecksum.not)) {
						var packed = [];
						128.do { |i|
							packed = packed.add(BlofeldSysex.pack(data[i*3], data[i*3+1], data[i*3+2]));
						};
						obj.addWave(Signal.newFrom(packed));
					};
				};
				slot = midiFile.sysexEvents[0][6].asInteger;
				displayName = midiFile.sysexEvents[0][393..406].asAscii;
			} {
				var sysexFile = SysexFile.read(path);
				sysexFile.events.do { |sysexEvent|
					// 0 WaldorfID
					// 1 BlofeldID
					// 2 DeviceID
					// 3 wavetableDump
					// 4 Slot
					// 5 Wave number
					// 6 Format
					// 7-390 Wave data
					// 391-404 Display Name
					// 405 0x00
					// 406 0x00
					// 407 Checksum
					var data = sysexEvent[7..390];
					var validChksum = (sysexEvent[407] == 0x7F) || (sysexEvent[407] == BlofeldSysex.checksum(data));
					if ((sysexEvent[0] == BlofeldSysex.waldorfID) && (sysexEvent[1] == BlofeldSysex.blofeldID) && (validChksum || checkChecksum.not)) {
						var packed = [];
						128.do { |i|
							packed = packed.add(BlofeldSysex.pack(data[i*3], data[i*3+1], data[i*3+2]));
						};
						obj.addWave(Signal.newFrom(packed));
					};
				};
				slot = sysexFile.events[0][4].asInteger;
				displayName = sysexFile.events[0][391..404].asAscii;
			};
			obj.name = name;
			obj.slot = slot;
			obj.displayName = displayName;
			if (obj.validate) {
				loaded.put(obj.name, obj);
			} {
				"Could not load wavetable: %".format(path).postln;
			}
		};
		^obj;
	}

	*newFrom { |levels, times, curve = 'lin'|
		var signal = Signal.newClear;
		64.do { |wave|
			signal = signal ++ Env(levels.value(wave), times.value(wave), curve.value(wave)).asSignal(128);
		};
		^this.new(signal);
	}

	*new { |signal, slot = 80, displayName = "User Wavetable"|
		^super.newCopyArgs(slot, signal, displayName);
	}

	addWave { |wave|
		signal = signal ++ wave.asSignal(128);
	}

	getWave { |i = 0|
		var numChannels = this.numChannels;
		^(signal[(128*i*numChannels)..(128*(i+1)*numChannels-1)])
	}

	numWaves {
		var numChannels = this.numChannels;
		^if (numChannels > 1, {
			(signal.size / 128 / numChannels).asInteger;
		}, {
			(signal.size / 128).asInteger;
		});
	}

	numChannels {
		^(signal.size / (128*64)).asInteger.max(1);
	}

	upload { |callback = nil|
		var numChannels = this.numChannels;
		this.checkForErrors;
		this.fixSignalSize;
		Routine({
			64.do({ |i|
				blofeld.wavetableDump(slot, signal[(128*i*numChannels)..(128*(i+1)*numChannels-1)], displayName.ascii, i, numChannels);
			});
			12.wait;
			if (callback != nil, { callback.value });
		}).play;
	}

	validate {
		^(this.validateSlot && this.validateSignalSize && this.validateDisplayName);
	}

	validateSlot {
		^((slot >= 80) && (slot <= 118));
	}

	validateSignalSize {
		^(signal.size > 0 && ((signal.size % 128) == 0));
	}

	validateDisplayName {
		^(displayName.size <= 14);
	}

	checkForErrors {
		if (this.validateSlot.not) {
			Error("Slot must be between 80 and 118.").throw;
		};
		if (this.validateSignalSize.not) {
			Error("Signal must have multitude of power of 2 of 128 samples").throw;
		};
		if (this.validateDisplayName.not) {
			Error("Name must be less than 14 ASCII characters long.").throw;
		};
	}

	fixSignalSize {
		while ({
			signal.size < (128*64);
		}, {
			var wavesMissing = (64 - this.numWaves);
			signal = signal ++ signal[(signal.size-1)..(signal.size-1-wavesMissing*128).max(0)];
		});
	}

	saveToFile { |path, fixSignalSize = false|
		var midiFile = SimpleMIDIFile.new(path);
		var numWaves = this.numWaves;
		var numChannels = this.numChannels;
		this.checkForErrors;
		this.fixSignalSize;
		numWaves.do { |i|
			var packet = BlofeldSysex.wavetableDumpPacket(slot, signal[(128*i*numChannels)..(128*(i+1)*numChannels-1)], displayName.ascii, i, numChannels, 0x00, false);
			// prefix with [track, absTime]
			midiFile.addSysexEvent([0, 0] ++ packet);
		};
		midiFile.write;
	}
}

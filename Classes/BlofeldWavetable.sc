BlofeldWavetable {
	var <slot;
	var <signal;
	var <name;
	var <multiplier = 1;

	*newFromFile { |path|
		var obj = this.new(Signal.newClear);
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
			// 9-137 Wave data
			// 138 0x00
			// 139 0x00
			// 140 Checksum
			// 141 -9
			var data = sysexEvent[9..137];
			if (sysexEvent[2] == BlofeldSysex.waldorfID && sysexEvent[3] == BlofeldSysex.blofeldID && sysexEvent[140] == BlofeldSysex.checksum(data), {
				obj.addWave(Signal.newFrom(data));
			});
		};
		obj.setSlot(midiFile.sysexEvents[0][6]);
		obj.setName(midiFile.sysexEvents[0][138..152].asAscii);
		^obj;
	}

	*newFrom { |levels, times, curve = 'lin'|
		var signal = Signal.newClear;
		64.do { |wave|
			signal = signal ++ Env(levels.value(wave), times.value(wave), curve.value(wave)).asSignal(128);
		};
		^this.new(signal);
	}

	*new { |signal, slot = 80, name = "User Wavetable"|
		^super.newCopyArgs(slot, signal, name);
	}

	setSlot { |value|
		slot = value
	}

	setName { |value|
		name = value;
	}

	addWave { |wave|
		signal = signal ++ wave.asSignal(128);
	}

	upload { |midiOut, deviceID = 0x00|
		this.validate;
		64.do({ |i|
			midiOut.sysex(BlofeldSysex.wavetableDumpPacket(slot, signal[(128*i*multiplier)..(128*(i+1)*multiplier-1)], name.ascii, i, multiplier, deviceID));
		});
	}

	validate {
		if (slot < 80 || slot > 118, {
			Error("Slot must be between 80 and 118.").throw;
		});
		if ((signal.size % 128) != 0, {
			Error("Signal must have multitude of power of 2 of 128 samples").throw;
		}, {
			if (signal.size < (128*64), {
				var div = signal.size / 128;
				var times = 6;
				while ({ div % 2 == 0 }, {
					div = div / 2;
					times = times - 1;
				});
				if (div != 1, {
					Error("Signal must have multitude of power of 2 of 128 samples").throw;
				});
				times.do {
					signal = signal ++ signal;
				};
			}, {
				multiplier = (signal.size / (128*64)).asInteger;
			});
		});
		if (name.size > 14, {
			Error("Name must be less than 14 ASCII characters long.").throw;
		});
	}

	saveToFile { |path|
		var midiFile = SimpleMIDIFile.new(path);
		this.validate;
		64.do { |i|
			var packet = BlofeldSysex.wavetableDumpPacket(slot, signal[(128*i*multiplier)..(128*(i+1)*multiplier-1)], name.ascii, i, multiplier, 0x00, false);
			// prefix with [track, absTime]
			midiFile.addSysexEvent([0, 0] ++ packet);
		};
		midiFile.write;
	}
}
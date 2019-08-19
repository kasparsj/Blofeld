BlofeldEditBuffer {
	const <editBufferBank = 0x7F;

	var <blofeld;
	var <parts;

	*new { |blofeld|
		^super.newCopyArgs(blofeld, ());
	}

	get { |param, location = 0|
		var sound = parts[location];
		var value = if (sound != nil, { sound.get(param) }, { nil });
		^value;
	}

	set { |param, value = 0, location = 0, useCache = false|
		var bParam = BlofeldParam.byName[param];
		var sound = parts[location];
		var uploadChange = useCache.not || (sound == nil);
		if (bParam == nil, {
			Error("Invalid param %".format(param)).throw;
		});
		if (bParam.sysex == nil, {
			Error("For global or control params use setGlobalParam or setControlParam").throw;
		});
		value = bParam.value(value.asInteger);
		if (sound == nil, {
			sound = BlofeldSound.new(editBufferBank, location);
		});
		uploadChange = uploadChange || (sound.data[bParam.sysex] != value);
		if (uploadChange, {
			if (bParam.control != nil, {
				blofeld.midiOut.control(location, bParam.control, value);
			}, {
				blofeld.midiOut.sysex(BlofeldSysex.paramChangePacket(bParam, value, location, blofeld.deviceID));
			});
		});
		sound.data[bParam.sysex] = value;
		if (useCache, {
			parts.put(location, sound);
		});
		^value;
	}

	download { |callback = nil, location = 0|
		BlofeldSysex.soundDumpCallback.put(BlofeldEditBuffer.key(location), this.expect(callback));
		blofeld.midiOut.sysex(BlofeldSysex.soundRequestPacket(editBufferBank, location, blofeld.deviceID));
	}

	upload { |sounds, callback = nil, location = 0|
		if (sounds.isArray.not, {
			sounds = [sounds];
		});
		^Routine({
			sounds.do { |sound, i|
				sound = sound.copy;
				sound.bank = editBufferBank;
				sound.program = location + i;
				blofeld.midiOut.sysex(BlofeldSysex.soundDumpPacket(sound, blofeld.deviceID));
				1.wait;
			};
			if (callback != nil, { callback.value });
		}).play;
	}

	init { |callback = nil, location = 0|
		this.getOrCreatePart(location).init();
		^this.upload(parts[location], callback, location);
	}

	randomize { |callback, location = 0, group = \sysex|
		this.getOrCreatePart(location).randomize(group);
		^this.upload(parts[location], callback, location);
	}

	getPart { |location = 0|
		^parts[location];
	}

	getOrCreatePart { |location = 0|
		var sound = this.getPart(location);
		if (sound == nil, {
			sound = BlofeldSound.new(editBufferBank, location);
			parts.put(location, sound);
		});
		^sound;
	}

	clear { |ps = 0|
		if (ps.isArray, {
			ps.do { |p|
				parts.removeAt(p);
			};
		}, {
			parts.removeAt(ps);
		});
	}

	expect { |callback = nil|
		^{|location, data|
			var sound = this.getOrCreatePart(location);
			sound.data = data;
			if (callback != nil, { callback.value(sound); });
			BlofeldSysex.soundDumpCallback.removeAt(BlofeldEditBuffer.key(location));
		}
	}

	*key { |location = 0|
		^("editBuffer"++location).asSymbol;
	}
}
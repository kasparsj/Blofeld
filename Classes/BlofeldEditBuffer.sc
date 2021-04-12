BlofeldEditBuffer {
	const <editBufferBank = 0x7F;

	var <blofeld;
	var <parts;
	var <routines;
	var <>multi;

	*new { |blofeld|
		^super.newCopyArgs(blofeld, Array.newClear(16), Array.newClear(16), nil);
	}

	get { |param, location = 0|
		var sound = parts[location];
		var value = if (sound != nil, { sound.get(param) }, { nil });
		^value;
	}

	set { |param, value = 0, location = 0, useCache = false|
		var bParam = BlofeldSound.byName[param];
		if (bParam != nil) {
			^this.setSoundParam(bParam, value, location, useCache);
		} {
			bParam = BlofeldMulti.byName[param];
			if (bParam != nil) {
				^this.setMultiParam(bParam, value);
			} {
				Error("Invalid param %".format(param)).throw;
			};
		};
	}

	fromTo { |param, startValue, endValue, duration, location = 0, curve = \lin, step = 0.1|
		var r = routines[location];
		if (r != nil) {
			if (r[param] != nil) {
				r[param].stop;
				r.removeAt(param);
			};
		} {
			routines[location] = ();
			r = routines[location];
		};
		r[param] = {
			var env = Env([startValue, endValue], [duration], curve);
			var time = 0;
			while ({time <= duration}, {
				this.set(param, env.at(time), location);
				step.wait;		
				time = time + step;
			});
			r.removeAt(param);
		}.fork;
		^r[param];
	}

	to { |param, endValue, duration, location = 0, curve = \lin, step = 0.1|
		^this.fromTo(param, this.get(param, location), endValue, duration, location, curve, step);
	}

	from { |param, startValue, duration, location = 0, curve = \lin, step = 0.1|
		^this.fromTo(param, startValue, this.get(param, location), duration, location, curve, step);
	}

	setSoundParam { |bParam, value = 0, location = 0, useCache = false|
		var sound = parts[location];
		var uploadChange = useCache.not || (sound == nil);
		value = bParam.value(value);
		if (sound == nil, {
			sound = BlofeldSound.new(editBufferBank, location);
		});
		uploadChange = uploadChange || (sound.data[bParam.sysex] != value);
		if (uploadChange, {
			blofeld.paramChange(bParam, value, location);
		});
		sound.data[bParam.sysex] = value;
		if (useCache, {
			parts.put(location, sound);
		});
		^value;
	}

	setMultiParam { |bParam, value = 0|
		value = bParam.value(value.asInteger);
		^if (multi == nil, {
			this.downloadMulti({
				{
					1.wait;
					this.doSetMultiParam_(bParam, value);
				}.fork;
			});
			value;
		}, {
			this.doSetMultiParam_(bParam, value);
		});
	}

	doSetMultiParam_ { |bParam, value|
		var uploadChange = (multi.data[bParam.sysex] != value);
		multi.data[bParam.sysex] = value;
		if (uploadChange, {
			this.uploadMulti;
		});
		^value;
	}

	download { |callback = nil, location = 0|
		blofeld.soundRequest(editBufferBank, location, this.onSoundDump(callback));
	}

	downloadMulti { |callback = nil|
		blofeld.multiRequest(0, editBufferBank, this.onMultiDump(callback));
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
				blofeld.soundDump(sound);
				1.wait;
			};
			if (callback != nil, { callback.value });
		}).play;
	}

	uploadMulti { |callback = nil|
		^Routine({
			blofeld.multiDump(multi);
			2.wait;
			if (callback != nil, { callback.value });
		}).play;
	}

	init { |callback = nil, location = 0, num = 1|
		location = location ? 0;
		num.do { |i|
			this.getOrCreatePart((location + i)).init;
		}
		^this.upload(parts[location..(location+num-1)], callback, location);
	}

	randomize { |callback = nil, location = 0, group = nil, num = 1|
		location = location ? 0;
		num.do { |i|
			this.getOrCreatePart((location + i)).randomize(group);
		}
		^this.upload(parts[location..(location+num-1)], callback, location);
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
				parts[p] = nil;
			};
		}, {
			parts[ps] = nil;
		});
	}

	onSoundDump { |callback = nil|
		^{|bank, location, data|
			var sound = this.getOrCreatePart(location);
			sound.data = data;
			if (callback != nil, { callback.value(sound); });
		}
	}

	onMultiDump { |callback = nil|
		^{|slot, bank, data|
			if (multi == nil) {
				multi = BlofeldMulti.new(0, editBufferBank, data);
			} {
				multi.data = data;
			};
			if (callback != nil, { callback.value(this.multi); });
		}
	}
}
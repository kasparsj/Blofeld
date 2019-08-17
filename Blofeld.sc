Blofeld {
	const sysexBegin = 0xF0;
	const sysexEnd = 0xF7;
	const waldorfID = 0x3E;
	const blofeldID = 0x13;
	const soundRequest = 0x00;
	const globalRequest = 0x04;
	const soundDump = 0x10;
	const wavetableDump = 0x12;
	const globalDump = 0x14;
	const paramChange = 0x20;
	const <editBuffer = 0x7F;

	classvar <bank;
	classvar <shape;
	classvar <lfoShape;
	classvar <arpMode;
	classvar <glideMode;
	classvar <filterType;
	classvar <category;
	classvar <effect;
	classvar <fmSource;
	classvar <modSource;
	classvar <modDest;
	classvar <initSoundData;
	classvar <numInstances = 0;

	var <>deviceID;
	var <sounds;
	var <global;
	var <callbacks;
	var <midiOut;

	*initClass {
		this.initDictionaries();

		Event.addEventType(\blofeld, { |server|
			var sendGlobal = false;
			currentEnvironment.keys.do({ |key|
				var bParam = BlofeldParam.byName[key];
				if (bParam != nil, {
					if (bParam.isGlobal, {
						~blofeld.setGlobalParam(key, currentEnvironment[key]);
						sendGlobal = true;
					}, {
						var chan = if (~chan == nil, { 0 }, { ~chan });
						var useCache = if (~useCache == nil, { true }, { ~useCache });
						~blofeld.setParam(key, currentEnvironment[key], chan, useCache);
					});
				});
			});
			if (sendGlobal, {
				~blofeld.sendGlobal();
			});
			if  (~midicmd != nil, {
				if (~midiout == nil, { ~midiout = ~blofeld.midiOut; });
				~eventTypes[\midi].value(server);
			});
		});
	}

	*new { |deviceID = 0|
		var instance = super.newCopyArgs(deviceID, (), Int8Array.new, ());
		if (numInstances == 0, {
			instance.makeDefault();
		});
		numInstances = numInstances + 1;
		^instance;
	}

	connect { |deviceName, portName, forceInit = false|
		if (MIDIClient.initialized == false || forceInit, {
			MIDIClient.init;
		});
		MIDIClient.sources.do({|endpoint, i|
			if (endpoint.device == deviceName && endpoint.name == portName, {
				MIDIIn.connect(i, endpoint);
			});
		});
		MIDIIn.addFuncTo(\sysex, {|src, sysex| this.parseSysex(sysex); });
		midiOut = MIDIOut.newByName(deviceName, portName);
	}

	makeDefault {
		Event.addParentType(\blofeld, (blofeld: this));
	}

	parseSysex { |packet|
		switch (packet[4],
			soundDump, {
				var bank = packet[5];
				var program = packet[6];
				var key = this.getKey(bank, program);
				this.getOrCreateSound(bank, program).data = packet[7..389];
				if (callbacks[key] != nil, {
					callbacks[key].value;
					callbacks.removeAt(key);
				});
			},
			globalDump, {
				global = packet[5..76];
				if (callbacks[\global] != nil, {
					callbacks[\global].value;
					callbacks.removeAt(\global);
				});
			}
		);
	}

	getOrCreateSound { |bank = 0x7F, program = 0x00|
		var key = this.getKey(bank, program);
		var sound = sounds[key];
		if (sound == nil, {
			sound = this.createSound(bank, program);
		});
		^sound;
	}

	getSound { |bank = 0x7F, program = 0x00|
		^sounds[this.getKey(bank, program)];
	}

	createSound { |bank = 0x7F, program = 0x00|
		var key = this.getKey(bank, program);
		var sound = BlofeldSound.new(bank, program);
		sounds.put(key, sound);
		^sound;
	}

	getKey { |bank = 0x7F, program = 0x00|
		var key = "b" ++ bank.asHexString(2) ++ "p" ++ program.asHexString(2);
		^key.asSymbol;
	}

	noteOn { |note = 60, veloc = 64, chan = 0|
		midiOut.noteOn(chan, note, veloc);
	}

	noteOff { |note = 60, veloc = 64, chan = 0|
		midiOut.noteOff(chan, note, veloc);
	}

	selectSound { |bank, program, chan = 0|
		this.setBank(bank, chan);
		this.setProgram(program, chan);
	}

	requestSound { |callback = nil, bank = 0x7F, program = 0x00|
		if (callback != nil, {
			var key = this.getKey(bank, program);
			callbacks.put(key, callback);
		});
		midiOut.sysex(this.soundRequestPacket(bank, program));
	}

	initSound { |bank = 0x7F, program = 0x00|
		var sound = this.createSound(bank, program);
		midiOut.sysex(this.soundDumpPacket(sound));
	}

	randomizeSound { |group = \sysex, bank = 0x7F, program = 0x00|
		var sound = this.getOrCreateSound(bank, program);
		sound.randomize(group);
		midiOut.sysex(this.soundDumpPacket(sound));
	}

	setBank { |bank, chan = 0|
		this.setControlParam(\bankLSB, bank, chan);
		//if (this.multiMode.asBoolean, {
		//	this.setControlParam(\bankMSB, 127, chan);
		//});
	}

	setProgram { |num, chan = 0|
		midiOut.program(chan, num);
		sounds.removeAt(this.getKey());
	}

	getParam { |param, location = 0|
		var sound = this.getSound(editBuffer, location);
		var value = if (sound != nil, { sound.getParam(param) }, { nil });
		^value;
	}

	setParam { |param, value = 0, location = 0, useCache = false|
		var bParam = BlofeldParam.byName[param];
		var sound = this.getSound(editBuffer, location);
		var sendValue = useCache.not;
		if (bParam == nil, {
			Error("Invalid param %".format(param)).throw;
		});
		if (bParam.sysex == nil, {
			Error("For global or control params use setGlobalParam or setControlParam").throw;
		});
		value = value.asInteger.min(127).max(0);
		sendValue = (sendValue || if (sound == nil, { true }, { sound.data[bParam.sysex] != value }));
		if (sendValue, {
			if (bParam.control != nil, {
				midiOut.control(location, bParam.control, value);
			}, {
				midiOut.sysex(this.paramChangePacket(bParam, value, location));
			});
		});
		if (sound != nil, {
			sound.data[bParam.sysex] = value;
		}, {
			if (useCache, {
				this.createSound(editBuffer, location).data[bParam.sysex] = value;
			});
		});
	}

	requestGlobal { |callback = nil|
		if (callback != nil, {
			callbacks.put(\global, callback);
		});
		midiOut.sysex(this.globalRequestPacket());
	}

	sendGlobal {
		midiOut.sysex(this.globalDumpPacket());
	}

	getGlobalParam { |param|
		var bParam = BlofeldParam.byName[param];
		var value = if (bParam != nil && bParam.sysex != nil, { global[bParam.sysex] }, { nil });
		^value;
	}

	setGlobalParam { |param, value = 0, sendGlobal = false|
		var bParam = BlofeldParam.byName[param];
		value = value.asInteger.min(127).max(0);
		if (bParam != nil && bParam.sysex != nil, {
			global[bParam.sysex] = value;
			if (sendGlobal, {
				this.sendGlobal();
			});
		});
	}

	setControlParam { |param, value, chan = 0|
		var bParam = BlofeldParam.byName[param];
		if (bParam != nil && bParam.control != nil, {
			midiOut.control(chan, bParam.control, value.asInteger.min(127).max(0));
		});
	}

	multiMode { |value = nil, sendGlobal = true|
		var bParam = BlofeldParam.byName[\multiMode];
		value = if (value != nil, {
			//this.setGlobalParam(\multiMode, value, sendGlobal);
			Error("Multimode needs to be switched on manually").throw;
		}, {
			global[bParam.sysex];
		});
		^value;
	}

	catFilter { |value = nil, sendGlobal = true|
		var bParam = BlofeldParam.byName[\catFilter];
		value = if (value != nil, {
			this.setGlobalParam(\catFilter, value+1, sendGlobal);
		}, {
			global[bParam.sysex];
		});
		^value;
	}

	createWavetable { |levels, times, curve = 'lin'|
		var signal = Signal.newClear;
		64.do { |wave|
			signal = signal ++ Env(levels.value(wave), times.value(wave), curve.value(wave)).asSignal(128);
		};
		^signal;
	}

	sendWavetable { |slot, signal, name|
		var mult = 1;
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
				mult = (signal.size / (128*64)).asInteger;
			});
		});
		if (name.size > 14, {
			Error("Name must be less than 14 ASCII characters long.").throw;
		});
		64.do({ |i|
			midiOut.sysex(this.wavetableDumpPacket(slot, signal[(128*i*mult)..(128*(i+1)*mult-1)], name.ascii, i, mult));
		});
	}

	soundRequestPacket { |bank, program|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(soundRequest);
		packet = packet.add(bank);
		packet = packet.add(program);
		packet = packet.add(sysexEnd);
		^packet;
	}

	soundDumpPacket { |sound|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(soundDump);
		packet = packet.add(sound.bank);
		packet = packet.add(sound.program);
		packet = packet.addAll(sound.data);
		packet = packet.add(this.checksum(sound.data));
		packet = packet.add(sysexEnd);
		^packet;
	}

	paramChangePacket { |param, value, location = 0|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(paramChange);
		packet = packet.add(location);
		packet = packet.add(param.sysex / 128);
		packet = packet.add(param.sysex % 128);
		packet = packet.add(value);
		packet = packet.add(sysexEnd);
		^packet;
	}

	globalRequestPacket {
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(globalRequest);
		packet = packet.add(sysexEnd);
		^packet;
	}

	globalDumpPacket {
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(globalDump);
		packet = packet.addAll(global);
		packet = packet.add(this.checksum(global));
		packet = packet.add(sysexEnd);
		^packet;
	}

	wavetableDumpPacket { |slot, samples, ascii, wave, mult = 1|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(wavetableDump);
		packet = packet.add(0x50 + (slot - 80));
		packet = packet.add(wave & 0x7F);
		packet = packet.add(0x00); // format
		128.do({ |i|
			var sample = (samples[i*mult] * 1048575).asInteger;
			// packet = packet.add((sample >> 14) & 0x7f);
			// packet = packet.add((sample >> 7) & 0x7f);
			// packet = packet.add((sample) & 0x7f);
			if (sample < 0, {
				packet = packet.add((sample & 0x000FC000) >> 14 + 0x40);
				}, {
					packet = packet.add((sample & 0x000FC000) >> 14);
			});
			packet = packet.add((sample & 0x00003F80) >> 7);
			packet = packet.add((sample & 0x0000007F));
		});
		14.do({ |i|
			var char = if(ascii[i] != nil, { ascii[i] & 0x7f }, { 0x00 });
			packet = packet.add(char);
		});
		packet = packet.add(0x00); // reserved
		packet = packet.add(0x00); // reserved
		packet = packet.add(this.checksum(packet[7..407]));
		packet = packet.add(sysexEnd);
		^packet;
	}

	checksum { |data|
		var csum = data.sum & 0x7F;
		^csum;
	}

	*initDictionaries {
		bank = (a: 0, b: 1, c: 2, d: 3, e: 4, f: 5, g: 6, h: 7);
		shape = (
			off: 0,
			pulse: 1,
			saw: 2,
			triangle: 3,
			sine: 4,
			alt1: 5,
			alt2: 6,
			resonant: 7,
			resonant2: 8,
			malletSyn: 9,
			sqrSweep: 10,
			bellish: 11,
			pulSweep: 12,
			sawSweep: 13,
			mellowSaw: 14,
			feedback: 15,
			addHarm: 16,
			reso3HP: 17,
			windSyn: 18,
			highHarm: 19,
			clipper: 20,
			organSyn: 21,
			squareSaw: 22,
			formant1: 23,
			polated: 24,
			transient: 25,
			electricP: 26,
			robotic: 27,
			strongHrm: 28,
			percOrgan: 29,
			clipSweep: 30,
			resoHarms: 31,
			twoEchos: 32,
			formant2: 33,
			fmntVocal: 34,
			microSync: 35,
			microPWM: 36,
			glassy: 37,
			squareHP: 38,
			sawSync1: 39,
			sawSync2: 40,
			sawSync3: 41,
			pulSync1: 42,
			pulSync2: 43,
			pulSync3: 44,
			sinSync1: 45,
			sinSync2: 46,
			sinSync3: 47,
			pwmPulse: 48,
			pwmSaw: 49,
			fuzzWave: 50,
			distorted: 51,
			heavyFuzz: 52,
			fuzzSynv: 53,
			kStrong1: 54,
			kStrong2: 55,
			kStrong3: 56,
			oneDashFive:57,
			nineteenTwenty: 58,
			waveTrip1: 59,
			waveTrip2: 60,
			waveTrip3: 61,
			waveTrip4: 62,
			maleVoice: 63,
			lowPiano: 64,
			resoSweep: 65,
			xmasBell: 66,
			fmPiano: 67,
			fatOrgan: 68,
			vibes: 69,
			chorus2: 70,
			truePWM: 71,
			upperWaves: 72,
			user1: 86,
			user2: 87,
			user3: 88,
			user4: 89,
			user5: 90,
			user6: 91,
			user7: 92,
			user8: 93,
			user9: 94,
			user10: 95,
			user11: 96,
			user12: 97,
			user13: 98,
			user14: 99,
		);
		lfoShape = (sine: 0, triangle: 1, square: 2, saw: 3, random: 4, sandh: 5);
		arpMode = (off: 0, on: 1, oneshot: 2, hold: 3);
		glideMode = (portamento: 0, fingeredp: 1, glissando: 2, fingeredg: 3);
		category = (
			init: 0,
			arp: 1,
			atmp: 2,
			bass: 3,
			drum: 4,
			fx: 5,
			keys: 6,
			lead: 7,
			mono: 8,
			pad: 9,
			perc: 10,
			poly: 11,
			seq: 12,
		);
		filterType = (
			bypass:0,
			lp24db:1,
			lp12db:2,
			bp24db:3,
			bp12db:4,
			hp24db:5,
			hp12db:6,
			notch24db:7,
			notch12db:8,
			combp:9,
			combm:10,
			ppglp: 11,
		);
		effect = (
			bypass: 0,
			chorus: 1,
			flanger: 2,
			phaser: 3,
			overdrive: 4,
			tripleFX: 5,
			delay: 6,
			clkDelay: 7,
			reverb: 8,
		);
		fmSource = (
			off: 0,
			osc1: 1,
			osc2: 2,
			osc3: 3,
			noise: 4,
			lfo1: 5,
			lfo2: 6,
			lfo3: 7,
			filterEnv: 8,
			ampEnv: 9,
			env3: 10,
			env4: 11,
		);
		modSource = (
			off: 0,
			lfo1: 1,
			lfo1mw: 2,
			lfo2: 3,
			lfo2press: 4,
			lfo3: 5,
			filterEnv: 6,
			ampEnv: 7,
			env3: 8,
			env4: 9,
			keytrack: 10,
			velocity: 11,
			relVelo: 12,
			pressure: 13,
			polyPress: 14,
			pitchBend: 15,
			modWheel: 16,
			sustain: 17,
			footCtrl: 18,
			breathCtrl: 19,
			controlW: 20,
			controlX: 21,
			controlY: 22,
			controlZ: 23,
			unisonoV: 24,
			modifier1: 25,
			modifier2: 26,
			modifier3: 27,
			modifier4: 28,
			minimum: 29,
			maximum: 30,
		);
		modDest = (
			pitch: 0,
			osc1Pitch: 1,
			osc1FM: 2,
			osc1PWWave: 3,
			osc2Pitch: 4,
			osc2FM: 5,
			osc2PWWave: 6,
			osc3Pitch: 7,
			osc3FM: 8,
			osc3PW: 9,
			osc1Level: 10,
			osc1Balance: 11,
			osc2Level: 12,
			osc2Balance: 13,
			osc3Level: 14,
			osc3Balance: 15,
			rmodLevel: 16,
			rmodBalance: 17,
			noiseLevel: 18,
			noiseBalance: 19,
			f1Cutoff: 20,
			f1Resonance: 21,
			f1FM: 22,
			f1Drive: 23,
			f1Pan: 24,
			f2Cutoff: 25,
			f2Resonance: 26,
			// TBC...
		);
	}
}
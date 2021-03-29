Blofeld {
	const <allSoundsBank = 0x40;
	const <allMultisBank = 0x10;

	classvar <bank;
	classvar <shape;
	classvar <fBalance;
	classvar <osc3Shape;
	classvar <lfoShape;
	classvar <arpMode;
	classvar <arpDirection;
	classvar <arpClock;
	classvar <glideMode;
	classvar <filterType;
	classvar <category;
	classvar <catFilter;
	classvar <effect1Type;
	classvar <effect2Type;
	classvar <fmSource;
	classvar <modSource;
	classvar <modSourceB;
	classvar <modDest;
	classvar <driveCurve;
	classvar <filterRouting;
	classvar <envMode;
	classvar <envTrigger;
	classvar <operator;
	classvar <allocation;
	classvar <unisono;
	classvar <m64p63;
	classvar <m24p24;
	classvar <m200p196perc;
	classvar <onOff;
	classvar <noise;
	classvar <ascii;
	classvar <octave;
	classvar <channel;
	classvar <tempo;
	classvar <numInstances = 0;
	classvar <>defaultChan = 0;

	var <>deviceID;
	var <sounds;
	var <multis;
	var <global;
	var <editBuffer;
	var <midiOut;
	var <midiOn;
	var <midiOff;
	var soundBrowser;
	var wavetableBrowser;

	*initClass {
		Class.initClassTree(Event);

		this.initDictionaries();

		Event.addEventType(\blofeld, { |server|
			var chan = ~chan ? Blofeld.defaultChan;
			var useCache = ~useCache ? true;
			var uploadGlobal = false;
			currentEnvironment.keys.do({ |key|
				if ((BlofeldSound.byName[key] != nil) ||
					(BlofeldMulti.byName[key] != nil)
				) {
					~blofeld.editBuffer.set(key, currentEnvironment[key], chan, useCache);
				} {
					if (BlofeldGlobal.byName[key] != nil) {
						// todo: implement changed
						var changed = true;
						~blofeld.global.set(key, currentEnvironment[key]);
						uploadGlobal = (uploadGlobal || changed);
					};
				};
			});
			if (uploadGlobal) {
				~blofeld.global.upload();
			};
			if  (~midicmd != nil) {
				if (~midiout == nil, { ~midiout = ~blofeld.midiOut; });
				~eventTypes[\midi].value(server);
			};
		});
	}

	*new { |deviceID = 0|
		var instance = super.newCopyArgs(deviceID);
		numInstances = numInstances + 1;
		instance.init();
		^instance;
	}

	init {
		sounds = BlofeldSoundset.new(this);
		multis = BlofeldMultiset.new(this);
		global = BlofeldGlobal.new(this);
		editBuffer = BlofeldEditBuffer.new(this);
		if (Blofeld.numInstances == 1, {
			this.makeDefault();
		});
		CmdPeriod.add { this.deinit(); }
	}

	deinit {
		this.control(\allNotesOff);
		midiOn.free;
		midiOff.free;
	}

	connect { |deviceName, portName, forceInit = false|
		if (MIDIClient.initialized == false || forceInit, {
			MIDIClient.init;
		});
		// MIDIClient.sources.do({|endpoint, i|
		// 	if (endpoint.device == deviceName && endpoint.name == portName, {
		// 		MIDIIn.connect(i, endpoint);
		// 	});
		// });
		MIDIIn.connectAll;
		midiOut = MIDIOut.newByName(deviceName, portName);
	}

	isConnected {
		^(midiOut != nil);
	}

	midiFunc {
		midiOn = MIDIFunc.noteOn({ |vel, num, chan, src|
			this.noteOn(num, vel, chan);
		});
		midiOff = MIDIFunc.noteOff({ |vel, num, chan, src|
			this.noteOff(num, vel, chan);
		});
	}

	makeDefault {
		if (Main.versionAtLeast( 3, 9 )) {
		    Event.addParentType(\blofeld, (blofeld: this));
		};
		// todo: do we need to throw an error?
	}

	soundBrowser { |loadSoundsets = \all|
		soundBrowser = BlofeldSoundBrowser.new(this);
		if (loadSoundsets != nil) {
			SystemClock.sched(0.2, {
				if (loadSoundsets == \all) {
					BlofeldSoundset.loadAll;
				} {
					if (loadSoundsets.isArray.not, {
						loadSoundsets = [loadSoundsets];
					});
					loadSoundsets.do { |name|
						BlofeldSoundset.load(BlofeldSoundset.files[name]);
					};
				};
				{ soundBrowser.start(loadSoundsets) }.defer;
			});
		} {
			soundBrowser.start;
		};
	}

	wavetableBrowser {
		wavetableBrowser = BlofeldWavetableBrowser.new(this);
	}

	noteOn { |note = 60, veloc = 64, chan = 0|
		midiOut.noteOn(chan, note, veloc);
	}

	noteOff { |note = 60, veloc = 64, chan = 0|
		midiOut.noteOff(chan, note, veloc);
	}

	selectSound { |bank, program, chan = 0|
		this.setBank(bank, chan);
		this.program(program, chan);
	}

	selectRandomSound { |chan = 0|
		this.selectSound(rrand(0, Blofeld.bank.size-1), rrand(0, 127), chan);
	}

	download { |obj, callback = nil|
		switch (obj.class,
			BlofeldSound, {
				if (obj.isEditBuffer, {
					editBuffer.download(callback, obj.program);
				}, {
					sounds.download(obj, callback);
				});
			},
			BlofeldMulti, {
				if (obj.isEditBuffer, {
					editBuffer.downloadMulti(callback);
				}, {
					multis.download(obj, callback);
				});
			},
			BlofeldSoundset, {
				obj.blofeld = this;
				obj.downloadAll(callback);
			},
			BlofeldMultiset, {
				obj.blofeld = this;
				obj.downloadAll(callback);
			},
			BlofeldGlobal, {
				obj.blofeld = this;
				obj.download(callback);
			}, {
				Error("% download not supported".format(obj.class.asString)).throw;
			}
		);
	}

	upload { |obj, callback = nil|
		switch (obj.class,
			BlofeldSound, {
				if (obj.isEditBuffer, {
					editBuffer.upload(obj);
				}, {
					sounds.upload(obj);
				});
			},
			BlofeldMulti, {
				obj.blofeld = this;
				if (obj.isEditBuffer, {
					editBuffer.multi = obj;
					editBuffer.uploadMulti;
				}, {
					multis.upload(obj);
				});
			},
			BlofeldSoundset, {
				obj.blofeld = this;
				obj.uploadAll(callback);
			},
			BlofeldMultiset, {
				obj.blofeld = this;
				obj.uploadAll(callback);
			},
			BlofeldGlobal, {
				obj.blofeld = this;
				obj.upload(callback);
			},
			BlofeldWavetable, {
				obj.blofeld = this;
				obj.upload(callback);
			}, {
				Error("% upload not supported".format(obj.class.asString)).throw;
			};
		);
	}

	setBank { |bank, chan = 0|
		this.control(\bankLSB, bank, chan);
		//if (this.multiMode.asBoolean, {
		//	this.setControlParam(\bankMSB, 127, chan);
		//});
	}

	program { |num, chan = 0|
		midiOut.program(chan, num);
		editBuffer.clear;
	}

	control { |param, value = 0, chan = 0|
		var bParam = BlofeldControlParams.byName[param];
		if (bParam != nil) {
			midiOut.control(chan, bParam.control, bParam.value(value));
		};
	}

	sysex { |packet|
		packet = packet.addFirst(BlofeldSysex.sysexBegin);
		packet = packet.add(BlofeldSysex.sysexEnd);
		midiOut.sysex(packet);
	}

	paramChange { |bParam, value, location|
		if (bParam.control != nil, {
			midiOut.control(location, bParam.control, value);
		}, {
			this.sysex(BlofeldSysex.paramChangePacket(bParam, value, location, deviceID));
		});
	}

	soundRequest { |bank, slot, callback|
		var key = Blofeld.soundKey(bank, slot, deviceID);
		if (bank != allSoundsBank) {
			BlofeldSysex.callbacks.put(key, { |bank, slot, data|
				callback.value(bank, slot, data);
				BlofeldSysex.callbacks.removeAt(key);
			});
		} {
			Blofeld.bank.values.sort.do { |b|
				128.do { |i|
					BlofeldSysex.callbacks.put(key, { |bank, slot, data|
						callback.value(bank, slot, data);
						BlofeldSysex.callbacks.removeAt(key);
					});
				}
			};
		};
		this.sysex(BlofeldSysex.soundRequestPacket(bank, slot, deviceID));
	}

	soundDump { |sound|
		this.sysex(BlofeldSysex.soundDumpPacket(sound, deviceID));
	}

	multiRequest { |slot, bank, callback|
		var key = Blofeld.multiKey(bank, slot, deviceID);
		if (bank != allMultisBank) {
			BlofeldSysex.callbacks.put(key, { |slot, bank, data|
				callback.value(slot, bank, data);
				BlofeldSysex.callbacks.removeAt(key);
			});
		} {
			16.do { |i|
				BlofeldSysex.callbacks.put(key, { |slot, bank, data|
					callback.value(slot, bank, data);
					BlofeldSysex.callbacks.removeAt(key);
				});
			};
		};
		this.sysex(BlofeldSysex.multiRequestPacket(slot, bank, deviceID));
	}

	multiDump { |multi|
		this.sysex(BlofeldSysex.multiDumpPacket(multi, deviceID));
	}

	globalRequest { |callback|
		var key = Blofeld.globalKey(deviceID);
		BlofeldSysex.callbacks.put(key, { |data|
			callback.value(data);
			BlofeldSysex.callbacks.removeAt(key);
		});
		this.sysex(BlofeldSysex.globalRequestPacket(deviceID));
	}

	globalDump { |global|
		this.sysex(BlofeldSysex.globalDumpPacket(global, deviceID));
	}

	wavetableDump { |slot, samples, ascii, wave, mult|
		this.sysex(BlofeldSysex.wavetableDumpPacket(slot, samples, ascii, wave, mult, deviceID));
	}

	*key { |bank = 0x7F, program = 0x00|
		var label = this.bank.findKeyForValue(bank) ? bank.asString;
		var key = label ++ program;
		^key.asSymbol;
	}

	*soundKey { |bank, slot, deviceID|
		^("sound_%_%".format(this.key(bank, slot), deviceID)).asSymbol;
	}

	*multiKey { |bank, slot, deviceID|
		^("multi_%_%".format(this.key(bank, slot), deviceID)).asSymbol;
	}

	*globalKey { |deviceID|
		^("global_%".format(deviceID)).asSymbol;
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
		);
		39.do { |i|
			shape.put(("user"++(i+1)).asSymbol, 86+i);
		};
		osc3Shape = (
			off: 0,
			pulse: 1,
			saw: 2,
			triangle: 3,
			sine: 4,
		);
		lfoShape = (sine: 0, triangle: 1, square: 2, saw: 3, random: 4, sandh: 5);
		glideMode = (portamento: 0, fingeredp: 1, glissando: 2, fingeredg: 3);
		category = (
			init: 0,
			arp: 1,
			atmo: 2,
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
		catFilter = (
			off: 0,
			init: 1,
			arp: 2,
			atmp: 3,
			bass: 4,
			drum: 5,
			fx: 6,
			keys: 7,
			lead: 8,
			mono: 9,
			pad: 10,
			perc: 11,
			poly: 12,
			seq: 13,
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
		effect1Type = (
			bypass: 0,
			chorus: 1,
			flanger: 2,
			phaser: 3,
			overdrive: 4,
			tripleFX: 5,
		);
		effect2Type = (
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
		modSourceB = modSource.copy;
		modSourceB.removeAt(\off);
		modSourceB.put(\constant, 0);
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
			f2FM: 27,
			f2Drive: 28,
			f2Pan: 29,
			volume: 30,
			lfo1Speed: 31,
			lfo2Speed: 32,
			// todo: TBC
		);
		driveCurve = (
			\clipping: 0,
			\tube: 1,
			\hard: 2,
			\medium: 3,
			\soft: 4,
			\pickup1: 5,
			\pickup2: 6,
			\rectifier: 7,
			\square: 8,
			\binary: 9,
			\overflow: 10,
			\sineShaper: 11,
			\osc1Mod: 12,
		);
		filterRouting = (
			\parallel: 0,
			\serial: 1,
		);
		envMode = (
			adsr: 0,
			ads1ds2r: 1,
			oneShot: 2,
			loopS1S2: 3,
			loopAll: 4,
		);
		envTrigger = (
			\normal: 0,
			\single: 1
		);
		operator = (
			\plus: 0,
			\minus: 1,
			\mult: 2,
			\and: 3,
			\or: 4,
			\xor: 5,
			\max: 6,
			\min: 7,
		);
		arpMode = (off: 0, on: 1, oneshot: 2, hold: 3);
		arpClock = (
			\one96: 0
			// todo: TBC
		);
		arpDirection = (
			up: 0,
			down: 1,
			altUp: 2,
			altDown: 3,
		);
		m64p63 = ();
		128.do { |i|
			var key = if ((i-64) > 0, { ("+"++(i-64)) }, { (i-64) }).asSymbol;
			m64p63.put(key, i);
		};
		m24p24 = ();
		49.do { |i|
			var key = if ((i-24) > 0, { ("+"++(i-24)) }, { (i-24) }).asSymbol;
			m24p24.put(key, 40+i);
		};
		m200p196perc = ();
		128.do { |i|
			var key = (if ((i-64) > 0, { ("+"++(i-64*3.125).asInteger) }, { (i-64*3.125).asInteger }).asString++"%").asSymbol;
			m200p196perc.put(key, i);
		};
		fBalance = ();
		128.do { |i|
			var key = if (i < 64, { "F1"+(i-64).abs }, {
				if (i == 64, {
					"middle"
				}, {
					("F2"+(i-64))
				});
			}).asSymbol;
			fBalance.put(key, i);
		};
		onOff = (
			off: 0,
			on: 1,
		);
		noise = (
			brown: 0,
			pink: 32,
			white: 64,
			blue: 96,
			violet: 127,
		);
		ascii = ();
		(32..127).do { |i|
			ascii.put(i.asAscii.asSymbol, i);
		};
		allocation = (
			poly: 0,
			mono: 1,
		);
		unisono = (
			off: 0,
			dual: 1,
			three: 2,
			four: 3,
			five: 4,
			six: 5,
		);
		octave = ();
		9.do { |i|
			octave.put(((128/(i*2)).asInteger.asString++"'").asSymbol, 16+(i*12));
		};
		channel = (
			global: 0,
			omni: 1,
		);
		16.do { |i|
			channel.put((i+1).asSymbol, (i+2));
		};
		tempo = ();
		128.do { |i|
			tempo.put((((260/127)*i).asInteger+40), i);
		};
	}
}

/*// todo: add dependency to Panola or remove
if (Quarks.isInstalled("Panola").not) {
	+ Panola {
		asBlofeldPbind {|chan = 0, props = nil|
			props = props ? [];
			^Pbindf(this.asPbind({}),
				\type, \blofeld,
				\chan, chan,
				*props.flatten
			);
		}
	}
};*/

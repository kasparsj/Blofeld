Blofeld {
	const sysexBegin = 0xF0;
	const sysexEnd = 0xF7;
	const waldorfID = 0x3E;
	const blofeldID = 0x13;
	const soundRequest = 0x00;
	const globalRequest = 0x04;
	const soundDump = 0x10;
	const globalDump = 0x14;
	const paramChange = 0x20;

	classvar <bank;
	classvar <shape;
	classvar <lfoShape;
	classvar <arpMode;
	classvar <glideMode;
	classvar <filterType;
	classvar <category;
	classvar <initSoundData;
	classvar <numInstances = 0;

	var <>deviceID;
	var <sounds;
	var <global;
	var <callbacks;
	var <midiOut;

	*initClass {
		bank = (a: 0, b: 1, c: 2, d: 3, e: 4, f: 5, g: 6, h: 7);
		shape = (pulse: 0, saw: 1, triangle: 2, sine: 3, alt1: 4, alt2: 5, resonant: 6, resonant2: 7);
		lfoShape = (sine: 0, triangle: 1, square: 2, saw: 3, rand: 4, sandh: 5);
		arpMode = (off: 0, on: 1, oneshot: 2, hold: 3);
		glideMode = (portamento: 0, fingeredp: 1, glissando: 2, fingeredg: 3);
		category = (init: 0, arp: 1, atmp: 2, bass: 3, drum: 4, fx: 5, keys: 6, lead: 7, mono: 8, pad: 9, perc: 10, poly: 11, seq: 12);
		filterType = (bypass:0, lp24db:1, lp12db:2, bp24db:3, bp12db:4, hp24db:5, hp12db:6, notch24db:7, notch12db:8, combp:9, combm:10, ppglp: 11);

		Event.addEventType(\blofeld, { |server|
			var sendGlobal = false;
			currentEnvironment.keys.do({ |key|
				var bParam = BlofeldParam.byName[key];
				if (bParam != nil, {
					if (bParam.isGlobal, {
						~blofeld.setGlobalParam(key, currentEnvironment[key]);
						sendGlobal = true;
					}, {
						~blofeld.setParam(key, currentEnvironment[key], if (~chan == nil, { 0 }, { ~chan }));
					});
				});
			});
			if (sendGlobal, {
				~blofeld.sendGlobal();
			});
		});
	}

	*new { |deviceName, portName, deviceID = 0|
		var instance = super.newCopyArgs(deviceID, (), Int8Array.new, ());
		instance.connect(deviceName, portName);
		if (numInstances == 0, {
			instance.makeDefault();
		});
		numInstances = numInstances + 1;
		^instance;
	}

	connect { |deviceName, portName|
		if (MIDIClient.initialized == false, {
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
			sound = BlofeldSound.new(bank, program);
			sounds.put(key, sound);
		});
		^sound;
	}

	getSound { |bank = 0x7F, program = 0x00|
		^sounds[this.getKey(bank, program)];
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

	// does not work sysex \multBank and \multiSound are readonly
	// selectMultiSound { |bank, program, chan = 0, sendGlobal = false|
	// 	this.setGlobalParam(("multiBank"++(chan+1)).asSymbol, bank);
	// 	this.setGlobalParam(("multiSound"++(chan+1)).asSymbol, program, sendGlobal);
	// }

	requestSound { |callback = nil, bank = 0x7F, program = 0x00|
		if (callback != nil, {
			var key = this.getKey(bank, program);
			callbacks.put(key, callback);
		});
		midiOut.sysex(this.soundRequestPacket(bank, program));
	}

	initSound { |bank = 0x7F, program = 0x00|
		var sound = this.getOrCreateSound(bank, program);
		sound.init();
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

	getParam { |param|
		var sound = this.getSound();
		var value = if (sound != nil, { sound.getParam(param) }, { nil });
		^value;
	}

	setParam { |param, value = 0, location = 0|
		var bParam = BlofeldParam.byName[param];
		var sound = this.getSound();
		value = value.asInteger;
		if (bParam != nil, {
			midiOut.sysex(this.paramChangePacket(bParam, value, location));
			if (sound != nil, { sound.data[bParam.sysex] = value; });
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
		value = value.asInteger;
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
			midiOut.control(chan, bParam.control, value.asInteger);
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

	checksum { |data|
		var csum = data.sum & 0x7F;
		^csum;
	}
}
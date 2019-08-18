BlofeldSound {
	classvar <initData;

	var <>bank;
	var <>program;
	var <>data;

	*initClass {
		initData = Int8Array.newFrom([
                   1, 64, 64, 64, 66, 96, 0, 0, 2, 127, 1, 64, 0, 0, 0, 0, 0, 64, 64, 64, 66,
                   96, 0, 0, 0, 127, 3, 64, 0, 0, 0, 0, 0, 52, 64, 64, 66, 96, 0, 0, 0, 127,
                   5, 64, 0, 0, 0, 0, 0, 0, 2, 64, 0, 0, 0, 0, 0, 20, 0, 0, 0, 127, 0, 127,
                   0, 127, 0, 0, 0, 64, 0, 0, 0, 0, 1, 0, 0, 1, 127, 64, 0, 0, 0, 0, 0, 0,
                   64, 64, 64, 1, 64, 0, 0, 64, 1, 64, 0, 0, 127, 64, 0, 0, 0, 0, 0, 0, 64,
                   64, 64, 0, 64, 0, 0, 64, 3, 64, 0, 0, 3, 0, 0, 127, 114, 5, 64, 0, 0, 0,
                   1, 0, 20, 64, 64, 0, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 8,
                   0, 53, 64, 100, 0, 64, 100, 0, 100, 110, 0, 15, 64, 127, 127, 0, 50, 64,
                   0, 0, 0, 0, 64, 0, 0, 64, 0, 0, 40, 64, 0, 0, 0, 0, 64, 0, 0, 64, 0, 0,
                   30, 64, 0, 0, 0, 0, 64, 0, 0, 64, 1, 0, 64, 0, 0, 127, 50, 0, 0, 127, 0,
                   0, 0, 0, 64, 0, 0, 127, 52, 127, 0, 127, 0, 0, 0, 0, 64, 0, 0, 64, 64, 64,
                   64, 64, 64, 0, 0, 0, 64, 0, 0, 64, 64, 64, 64, 64, 64, 0, 0, 1, 0, 0, 0,
                   64, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, 0, 64, 1, 1, 64, 0, 0, 64, 0, 0, 64,
                   0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0,
                   0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 0, 0, 64, 16, 100, 0, 0,
                   15, 8, 5, 0, 0, 0, 1, 12, 0, 0, 15, 0, 0, 55, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                   4, 4, 4, 4, 4, 4, 4, 68, 68, 68, 68, 68, 68, 68, 68, 68, 68, 68, 68, 68,
                   68, 68, 68, 68, 0, 0, 0, 73, 110, 105, 116, 32, 32, 32, 32, 32, 32, 32,
                   32, 32, 32, 32, 32, 0, 0, 0, 0]);
	}

	*new { |bank, program, data = nil|
		^super.newCopyArgs(bank, program, if (data == nil, { initData.copy }, { data }));
	}

	init {
		data = initData.copy;
	}

	randomize { |group = \sysex|
		BlofeldParam.groups[group].do({ |bParam|
			if (bParam.sysex != nil, {
				data[bParam.sysex] = bParam.rand;
			});
		});
	}

	getParam { |param|
		var bParam = BlofeldParam.byName[param];
		var value = if (bParam != nil, {
			if (bParam.sysex != nil, { data[bParam.sysex] });
		}, { nil });
		^value;
	}

	getLabel { |param|
		var bParam = BlofeldParam.byName[param];
		var label = if (bParam != nil, {
			if (bParam.sysex != nil, { bParam.label(data[bParam.sysex]); });
		}, { nil });
		^label;
	}

	isSet { |param|
		var bParam = BlofeldParam.byName[param];
		var set = if (bParam != nil, {
			if (bParam.sysex != nil, { bParam.isSet(data); });
		}, { false });
		^set;
	}

	randParam { |param|
		var bParam = BlofeldParam.byName[param];
		var value = nil;
		if (bParam != nil, {
			value = bParam.rand();
			data[bParam.sysex] = value;
		});
		^value;
	}

	printInfo { |fullInfo = false|
		this.printName();
		this.printCategory();
		this.printOsc("osc1", fullInfo);
		this.printOsc("osc2", fullInfo);
		this.printOsc("osc3", fullInfo);
		this.printFilter("filter1", fullInfo);
		this.printFilter("filter2", fullInfo);
		this.printParams([\filterRouting]);
		this.printAmp(fullInfo);
		this.printEnv("filterEnv", fullInfo);
		this.printEnv("ampEnv", fullInfo);
		this.printEnv("env3", fullInfo);
		this.printEnv("env4", fullInfo);
		3.do { |i|
			this.printLfo("lfo"++(i+1), fullInfo);
		};
		2.do { |i|
			if (this.isSet(("effect"++(i+1)++"Mix").asSymbol), {
				this.printEffect("effect"++(i+1), fullInfo);
			});
		};
		16.do { |i|
			if (this.isSet(("modulation"++(i+1)++"Amount").asSymbol), {
				this.printModulation("modulation"++(i+1), fullInfo);
			});
		};
		4.do { |i|
			if (this.isSet(("modifier"++(i+1)++"SourceA").asSymbol), {
				this.printModifier("modifier"++(i+1), fullInfo);
			});
		};
		this.printArpeggiator(fullInfo);
		this.printParams(BlofeldParam.paramSysex.collect({ |bParam|
			var skip = bParam.isOsc || bParam.isFilter || bParam.isAmplifier || bParam.isLfo || bParam.isEnvelope || bParam.isCategory || bParam.isName || bParam.isEffect || bParam.isArpeggiator || bParam.isModulation || bParam.isModifier;
			if (skip.not, {
				bParam.name;
			});
		}));
	}

	printParams { |params|
		params.do { |param|
			var bParam = BlofeldParam.byName[param];
			if (bParam != nil, {
				if (bParam.sysex != nil && bParam.isSet(data), {
					((bParam.name.asString ++ ":") + bParam.label(data[bParam.sysex])).postln;
				});
			});
		};
	}

	printName {
		var name = "";
		16.do { |i|
			var bParam = BlofeldParam.paramSysex[i+363];
			name = name ++ bParam.label(data[bParam.sysex]);
		};
		name.postln;
	}

	printCategory {
		var bParam = BlofeldParam.paramSysex[379];
		var name = "category:" + bParam.label(data[bParam.sysex]);
		name.postln;
	}

	printOsc { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Shape").asSymbol);
		msg = msg + this.getLabel((name++"Octave").asSymbol);
		msg = msg + this.getLabel((name++"Semitone").asSymbol);
		msg = msg + this.getLabel((name++"Detune").asSymbol);
		msg = msg + this.getLabel((name++"Level").asSymbol);
		msg = msg + this.getLabel((name++"Balance").asSymbol);
		msg.postln;
		if (fullInfo, {
			this.printParams([(name++"Pulsewidth").asSymbol, (name++"PWMSource").asSymbol, (name++"PWAmount").asSymbol, (name++"LimitWT").asSymbol, (name++"Brilliance").asSymbol, (name++"FMSource").asSymbol, (name++"FMAmount").asSymbol, (name++"Keytrack").asSymbol, (name++"BendRange").asSymbol]);
		});
	}

	printFilter { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Type").asSymbol);
		msg = msg + this.getLabel((name++"Cutoff").asSymbol);
		msg = msg + this.getLabel((name++"Resonance").asSymbol);
		msg = msg + this.getLabel((name++"EnvAmount").asSymbol);
		msg.postln;
		if (fullInfo, {
			this.printParams([(name++"Drive").asSymbol, (name++"DriveCurve").asSymbol, (name++"Keytrack").asSymbol, (name++"EnvVelocity").asSymbol, (name++"ModSource").asSymbol, (name++"ModAmount").asSymbol, (name++"FMSource").asSymbol, (name++"FMAmount").asSymbol, (name++"Pan").asSymbol, (name++"PanSource").asSymbol, (name++"PanAmount").asSymbol]);
		});
	}

	printAmp { |fullInfo = false|
		var msg = "amp:" + this.getLabel(\ampVolume) + this.getLabel(\ampVelocity);
		msg.postln;
		if (fullInfo, {
			this.printParams([\ampModSource, \ampModAmount]);
		});
	}

	printLfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Shape").asSymbol);
		msg = msg + this.getLabel((name++"Speed").asSymbol);
		msg.postln;
		if (fullInfo, {
			this.printParams([(name++"Sync").asSymbol, (name++"Clocked").asSymbol, (name++"StartPhase").asSymbol, (name++"Keytrack").asSymbol, (name++"Delay").asSymbol, (name++"Fade").asSymbol]);
		});
	}

	printEnv { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Mode").asSymbol);
		msg = msg + this.getLabel((name++"Attack").asSymbol);
		if (this.getParam((name++"AttackLevel").asSymbol) != 127, {
			msg = msg + "(" ++ this.getLabel((name++"AttackLevel").asSymbol) ++ ")";
		});
		msg = msg + this.getLabel((name++"Decay").asSymbol);
		msg = msg + this.getLabel((name++"Sustain").asSymbol);
		msg = msg + this.getLabel((name++"Release").asSymbol);
		if (fullInfo, {
			if (this.getParam((name++"Mode").asSymbol) == Blofeld.envMode[\ads1ds2r], {
				msg = msg + "/";
				msg = msg + this.getLabel((name++"Decay2").asSymbol);
				msg = msg + this.getLabel((name++"Sustain2").asSymbol);
			});
		});
		msg.postln;
	}

	printEffect { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Type").asSymbol);
		msg = msg + this.getLabel((name++"Mix").asSymbol);
		msg.postln;
		if (fullInfo, {
			var names = 14.collect({|i| (name++"Param"++(i+1)).asSymbol });
			this.printParams(names);
		});
	}

	printModulation { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Src").asSymbol);
		msg = msg + "->" + this.getLabel((name++"Amount").asSymbol);
		msg = msg + "->" + this.getLabel((name++"Dst").asSymbol);
		msg.postln;
	}

	printModifier { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"SourceA").asSymbol);
		msg = msg + "->" + this.getLabel((name++"Operation").asSymbol);
		msg = msg + "<-" + this.getLabel((name++"SourceB").asSymbol);
		msg.postln;
	}

	printArpeggiator { |fullInfo = false|
		var name = "arp";
		var msg = (name ++ ":") + this.getLabel((name++"Mode").asSymbol);
		msg = msg + this.getLabel((name++"Clock").asSymbol);
		msg = msg + this.getLabel((name++"Tempo").asSymbol);
		msg = msg + this.getLabel((name++"Pattern").asSymbol);
		msg = msg + this.getLabel((name++"Direction").asSymbol);
		msg.postln;
		if (fullInfo, {
			this.printParams([(name++"Octave").asSymbol, (name++"Length").asSymbol, (name++"SortOrder").asSymbol, (name++"TimingFactor").asSymbol, (name++"Velocity").asSymbol, (name++"PtnReset").asSymbol, (name++"ptnLength").asSymbol]);
		});
	}
}


BlofeldSound {
	classvar <initData;

	var <>bank;
	var <>program;
	var <>data;
	var <>soundset;

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

	*new { |bank, program, data = nil, soundset = nil|
		^super.newCopyArgs(bank, program, if (data == nil, { initData.copy }, { data }), soundset);
	}

	key {
		^if (this.isEditBuffer, {
			BlofeldEditBuffer.key(program);
		}, {
			Blofeld.key(bank, program);
		});
	}

	isEditBuffer {
		^(bank == BlofeldEditBuffer.editBufferBank);
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
		this.setName("random");
		this.setCategory(Blofeld.category[\init]);
	}

	get { |param|
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

	rand { |param|
		var bParam = BlofeldParam.byName[param];
		var value = nil;
		if (bParam != nil, {
			value = bParam.rand();
			data[bParam.sysex] = value;
		});
		^value;
	}

	getInfo { |fullInfo = false|
		var info = this.getName() ++ "\n" ++
		(if (soundset != nil, {
			"soundset:" + soundset.name.asString ++ "\n" ++
			"bank: " + bank + "program: " + program ++ "\n"
		}, { "" })) ++
		("category:" + this.getCategory()) ++ "\n" ++
		this.getOscInfo("osc1", fullInfo) ++ "\n" ++
		this.getOscInfo("osc2", fullInfo) ++ "\n" ++
		this.getOscInfo("osc3", fullInfo) ++ "\n" ++
		this.getFilterInfo("filter1", fullInfo) ++ "\n" ++
		this.getFilterInfo("filter2", fullInfo) ++ "\n";
		this.getParamInfo([\filterRouting]) ++
		this.getAmpInfo(fullInfo) ++ "\n" ++
		this.getEnvInfo("filterEnv", fullInfo) ++ "\n" ++
		this.getEnvInfo("ampEnv", fullInfo) ++ "\n" ++
		this.getEnvInfo("env3", fullInfo) ++ "\n" ++
		this.getEnvInfo("env4", fullInfo) ++ "\n";
		3.do { |i|
			info = info ++ this.getLfoInfo("lfo"++(i+1), fullInfo) ++ "\n";
		};
		2.do { |i|
			if (this.isSet(("effect"++(i+1)++"Mix").asSymbol), {
				info = info ++ this.getEffectInfo("effect"++(i+1), fullInfo) ++ "\n";
			});
		};
		16.do { |i|
			if (this.isSet(("modulation"++(i+1)++"Amount").asSymbol), {
				info = info ++ this.getModulationInfo("modulation"++(i+1), fullInfo) ++ "\n";
			});
		};
		4.do { |i|
			if (this.isSet(("modifier"++(i+1)++"SourceA").asSymbol), {
				info = info ++ this.getModifierInfo("modifier"++(i+1), fullInfo) ++ "\n";
			});
		};
		if (this.isSet(\arpMode), {
			info = info ++ this.getArpeggiatorInfo(fullInfo) ++ "\n";
		});
		this.getParamInfo(BlofeldParam.paramSysex.collect({ |bParam|
			var skip = bParam.isOsc || bParam.isFilter || bParam.isAmplifier || bParam.isLfo || bParam.isEnvelope || bParam.isCategory || bParam.isName || bParam.isEffect || bParam.isArpeggiator || bParam.isModulation || bParam.isModifier;
			if (skip.not, {
				bParam.name;
			});
		}));
		^info;
	}

	printInfo { |fullInfo = false|
		this.getInfo(fullInfo).postln;
	}

	getParamInfo { |params|
		var info = "";
		params.do { |param|
			var bParam = BlofeldParam.byName[param];
			if (bParam != nil, {
				if (bParam.sysex != nil && bParam.isSet(data), {
					info = info ++ ((bParam.name.asString ++ ":") + bParam.label(data[bParam.sysex])) ++ "\n";
				});
			});
		};
		^info;
	}

	getName {
		var name = "";
		16.do { |i|
			var bParam = BlofeldParam.paramSysex[i+363];
			name = name ++ bParam.label(data[bParam.sysex]);
		};
		^name;
	}

	setName { |value|
		16.do { |i|
			var bParam = BlofeldParam.paramSysex[i+363];
			data[bParam.sysex] = if (value[i] == nil, { 0 }, { value[i].ascii });
		}
	}

	getCategory {
		var bParam = BlofeldParam.paramSysex[379];
		^bParam.label(data[bParam.sysex]);
	}

	setCategory { |value|
		var bParam = BlofeldParam.paramSysex[379];
		data[bParam.sysex] = value;
	}

	getOscInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Shape").asSymbol);
		msg = msg + this.getLabel((name++"Octave").asSymbol);
		msg = msg + this.getLabel((name++"Semitone").asSymbol);
		msg = msg + this.getLabel((name++"Detune").asSymbol);
		msg = msg + this.getLabel((name++"Level").asSymbol);
		msg = msg + this.getLabel((name++"Balance").asSymbol);
		if (fullInfo, {
			msg = msg + this.printParams([(name++"Pulsewidth").asSymbol, (name++"PWMSource").asSymbol, (name++"PWAmount").asSymbol, (name++"LimitWT").asSymbol, (name++"Brilliance").asSymbol, (name++"FMSource").asSymbol, (name++"FMAmount").asSymbol, (name++"Keytrack").asSymbol, (name++"BendRange").asSymbol]);
		});
		^msg;
	}

	getFilterInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Type").asSymbol);
		msg = msg + this.getLabel((name++"Cutoff").asSymbol);
		msg = msg + this.getLabel((name++"Resonance").asSymbol);
		msg = msg + this.getLabel((name++"EnvAmount").asSymbol);
		if (fullInfo, {
			msg = msg + this.printParams([(name++"Drive").asSymbol, (name++"DriveCurve").asSymbol, (name++"Keytrack").asSymbol, (name++"EnvVelocity").asSymbol, (name++"ModSource").asSymbol, (name++"ModAmount").asSymbol, (name++"FMSource").asSymbol, (name++"FMAmount").asSymbol, (name++"Pan").asSymbol, (name++"PanSource").asSymbol, (name++"PanAmount").asSymbol]);
		});
		^msg;
	}

	getAmpInfo { |fullInfo = false|
		var msg = "amp:" + this.getLabel(\ampVolume) + this.getLabel(\ampVelocity);
		if (fullInfo, {
			msg = msg + this.printParams([\ampModSource, \ampModAmount]);
		});
		^msg;
	}

	getLfoInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Shape").asSymbol);
		msg = msg + this.getLabel((name++"Speed").asSymbol);
		if (fullInfo, {
			msg = msg + this.printParams([(name++"Sync").asSymbol, (name++"Clocked").asSymbol, (name++"StartPhase").asSymbol, (name++"Keytrack").asSymbol, (name++"Delay").asSymbol, (name++"Fade").asSymbol]);
		});
		^msg;
	}

	getEnvInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Mode").asSymbol);
		msg = msg + this.getLabel((name++"Attack").asSymbol);
		if (this.get((name++"AttackLevel").asSymbol) != 127, {
			msg = msg + "(" ++ this.getLabel((name++"AttackLevel").asSymbol) ++ ")";
		});
		msg = msg + this.getLabel((name++"Decay").asSymbol);
		msg = msg + this.getLabel((name++"Sustain").asSymbol);
		msg = msg + this.getLabel((name++"Release").asSymbol);
		if (fullInfo, {
			if (this.get((name++"Mode").asSymbol) == Blofeld.envMode[\ads1ds2r], {
				msg = msg + "/";
				msg = msg + this.getLabel((name++"Decay2").asSymbol);
				msg = msg + this.getLabel((name++"Sustain2").asSymbol);
			});
		});
		^msg;
	}

	getEffectInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Type").asSymbol);
		msg = msg + this.getLabel((name++"Mix").asSymbol);
		if (fullInfo, {
			var names = 14.collect({|i| (name++"Param"++(i+1)).asSymbol });
			msg = msg + this.printParams(names);
		});
		^msg;
	}

	getModulationInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"Src").asSymbol);
		msg = msg + "->" + this.getLabel((name++"Amount").asSymbol);
		msg = msg + "->" + this.getLabel((name++"Dst").asSymbol);
		^msg;
	}

	getModifierInfo { |name, fullInfo = false|
		var msg = (name ++ ":") + this.getLabel((name++"SourceA").asSymbol);
		msg = msg + "->" + this.getLabel((name++"Operation").asSymbol);
		msg = msg + "<-" + this.getLabel((name++"SourceB").asSymbol);
		^msg;
	}

	getArpeggiatorInfo { |fullInfo = false|
		var name = "arp";
		var msg = (name ++ ":") + this.getLabel((name++"Mode").asSymbol);
		msg = msg + this.getLabel((name++"Clock").asSymbol);
		msg = msg + this.getLabel((name++"Tempo").asSymbol);
		msg = msg + this.getLabel((name++"Pattern").asSymbol);
		msg = msg + this.getLabel((name++"Direction").asSymbol);
		if (fullInfo, {
			msg = msg + this.printParams([(name++"Octave").asSymbol, (name++"Length").asSymbol, (name++"SortOrder").asSymbol, (name++"TimingFactor").asSymbol, (name++"Velocity").asSymbol, (name++"PtnReset").asSymbol, (name++"ptnLength").asSymbol]);
		});
		^msg;
	}
}


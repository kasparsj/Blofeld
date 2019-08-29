BlofeldMulti {
	classvar <>params;
	classvar <>byName;
	classvar <>bySysex;

	var <slot;
	var <bank;
	var <>data;
	var <>multiset;

	*new { |slot, bank = 0x00, data = nil|
		^super.newCopyArgs(slot, bank, Array.newClear(16), data);
	}

	get { |param|
		var bParam = byName[param];
		var value = if (bParam != nil, {
			if (bParam.sysex != nil, { data[bParam.sysex] });
		}, { nil });
		^value;
	}

	getLabel { |param|
		var bParam = byName[param];
		var label = if (bParam != nil, {
			if (bParam.sysex != nil, { bParam.label(data[bParam.sysex]); });
		}, { nil });
		^label;
	}

	getName {
		var name = "";
		16.do { |i|
			var bParam = bySysex[i];
			name = name ++ bParam.label(data[bParam.sysex]);
		};
		^name;
	}

	asInt8Array {
		var packet = Int8Array.new;
		packet = packet.add(slot);
		packet = packet.addAll(data);
		^packet;
	}

	*initClass {
		Class.initClassTree(Blofeld);

		// multi data
		params = [
			BlofeldParam.new(\multiVolume, 17, nil, (0..127)),
			BlofeldParam.new(\multiTempo, 18, nil, Blofeld.tempo, 54),
		];
		// name
		16.do { |i|
			params = params.add(BlofeldParam.new(("multiName"++(i+1)).asSymbol, i, nil, Blofeld.ascii));
		};
		// 16 parts
		16.do { |i|
			var j = i+1;
			params = params.addAll([
				BlofeldParam.new(("bank"++j).asSymbol, 32+(24*i), nil, Blofeld.bank),
				BlofeldParam.new(("sound"++j).asSymbol, 33+(24*i), nil, (0..127)),
				BlofeldParam.new(("vol"++j).asSymbol, 34+(24*i), nil, (0..127)),
				BlofeldParam.new(("pan"++j).asSymbol, 35+(24*i), nil, Blofeld.m64p63, 64),
				BlofeldParam.new(("transpose"++j).asSymbol, 37+(24*i), nil, Blofeld.m64p63, 64),
				BlofeldParam.new(("detune"++j).asSymbol, 38+(24*i), nil, Blofeld.m64p63, 64),
				BlofeldParam.new(("chan"++j).asSymbol, 39+(24*i), nil, Blofeld.channel, (i+2)),
				BlofeldParam.new(("lowVel"++j).asSymbol, 42+(24*i), nil, (1..127), 1),
				BlofeldParam.new(("highVel"++j).asSymbol, 43+(24*i), nil, (1..127), 1),
			]);
		};

		BlofeldParam.initGroup(this);
	}
}
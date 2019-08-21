BlofeldMulti {
	const <allMultiBank = 0x10;

	var <slot;
	var <bank;
	var <>data;

	*new { |slot, bank = 0x00, data = nil|
		^super.newCopyArgs(slot, bank, data);
	}

	get { |param|
		var bParam = BlofeldParam.multiByName[param];
		var value = if (bParam != nil, {
			if (bParam.sysex != nil, { data[bParam.sysex] });
		}, { nil });
		^value;
	}

	getLabel { |param|
		var bParam = BlofeldParam.multiByName[param];
		var label = if (bParam != nil, {
			if (bParam.sysex != nil, { bParam.label(data[bParam.sysex]); });
		}, { nil });
		^label;
	}

	getName {
		var name = "";
		16.do { |i|
			var bParam = BlofeldParam.multiSysex[i];
			name = name ++ bParam.label(data[bParam.sysex]);
		};
		^name;
	}
}
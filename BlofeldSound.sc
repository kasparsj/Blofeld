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
		var value = if (bParam != nil && bParam.sysex != nil, { data[bParam.sysex] }, { nil });
		^value;
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

	printParams {
		data.do { |value, sysex|
			var bParam = BlofeldParam.paramSysex[sysex];
			if (bParam != nil, {
				if (data[sysex] != bParam.defaultValue, {
					(bParam.name.asString + bParam.label(value)).postln;
				});
			});
		};
	}
}

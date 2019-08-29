BlofeldParam {
	var <name;
	var <sysex;
	var <control;
	var <values;
	var <defaultValue;
	var <relParam;
	var <>groupClass;
	var <>sequential = true;

	*initGroup { |groupClass|
		groupClass.byName = ();
		groupClass.bySysex = ();
		groupClass.params.do { |param|
			param.groupClass = groupClass;
			groupClass.byName.put(param.name, param);
			groupClass.bySysex.put(param.sysex, param);
		};
	}

	*new { |name, sysex, control, values, defaultValue = 0, relParam|
		^super.newCopyArgs(name, sysex, control, values, defaultValue, relParam);
	}

	choose {
		^values.choose;
	}

	min {
		^values.minItem;
	}

	max {
		^values.maxItem;
	}

	nearest { |value|
		^values.asList.sort({ |a, b|
			(a - value).abs < (b - value).abs;
		})[0];
	}

	value { |value|
		^if (sequential, { value.min(this.max).max(this.min) }, { this.nearest(value) });
	}

	label { |value|
		var label = if (values.isArray, {
			value;
		}, {
			values.findKeyForValue(value);
		});
		^label;
	}

	isSet { |data|
		var set = if (data[sysex] != defaultValue, {
			if (relParam != nil, {
				groupClass.byName[relParam].isSet(data);
			}, {
				true;
			});
		}, {
			false;
		});
		^set;
	}

	isName {
		^name.asString.beginsWith("nameChar");
	}

	isCategory {
		^(name == \category);
	}

	isOsc {
		^(
			name.asString.beginsWith("osc1") ||
			name.asString.beginsWith("osc2") ||
			name.asString.beginsWith("osc3")
		);
	}

	isFilter {
		^(
			name.asString.beginsWith("filter1") ||
			name.asString.beginsWith("filter2") ||
			name.asString.beginsWith("filterRouting")
		);
	}

	isAmplifier {
		^(
			name.asString.beginsWith("ampV") ||
			name.asString.beginsWith("ampM")
		);
	}

	isLfo {
		^(
			name.asString.beginsWith("lfo1") ||
			name.asString.beginsWith("lfo2") ||
			name.asString.beginsWith("lfo3")
		);
	}

	isEnvelope {
		^(
			name.asString.beginsWith("filterEnv") ||
			name.asString.beginsWith("ampEnv") ||
			name.asString.beginsWith("env3") ||
			name.asString.beginsWith("env4")
		);
	}

	isEffect {
		^name.asString.beginsWith("effect");
	}

	isArpeggiator {
		^name.asString.beginsWith("arp");
	}

	isModulation {
		^name.asString.beginsWith("modulation");
	}

	isModifier {
		^name.asString.beginsWith("modifier");
	}

	asString {
		^name;
	}
}

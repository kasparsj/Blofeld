BlofeldGlobal {
	classvar <>params;
	classvar <>byName;
	classvar <>bySysex;

	var <blofeld;
	var <data;

	*new { |blofeld = nil|
		^super.newCopyArgs(blofeld, Int8Array.new, ());
	}

	get { |param|
		var bParam = byName[param];
		var value = if (bParam != nil && bParam.sysex != nil, { data[bParam.sysex] }, { nil });
		^value;
	}

	set { |param, value = 0, upload = false|
		var bParam = byName[param];
		value = value.asInteger.min(127).max(0);
		if (bParam != nil && bParam.sysex != nil, {
			data[bParam.sysex] = value;
			if (upload, {
				this.upload();
			});
		});
	}

	download { |callback = nil|
		blofeld.globalRequest(this.onGlobalDump(callback));
	}

	upload { |callback = nil|
		var r = Routine({
			blofeld.globalDump(this);
			if (callback != nil, {
				1.wait;
				callback.value;
			});
		});
		r.play;
		^r;
	}

	multiMode { |value = nil, upload = true|
		var bParam = byName[\multiMode];
		value = if (value != nil, {
			//this.setGlobalParam(\multiMode, value, sendGlobal);
			Error("Multimode needs to be switched on manually").throw;
		}, {
			data[bParam.sysex];
		});
		^value;
	}

	catFilter { |value = nil, upload = true|
		var bParam = byName[\catFilter];
		value = if (value != nil, {
			this.set(\catFilter, value, upload);
		}, {
			data[bParam.sysex];
		});
		^value;
	}

	onGlobalDump { |callback = nil|
		^{|packetData|
			data = packetData;
			if (callback != nil, { callback.value(this); });
		};
	}

	asInt8Array {
		^data;
	}

	*initClass {
		Class.initClassTree(Blofeld);

		params = [
			// global data
			BlofeldParam.new(\multiMode, 1, nil, (0..1)),
			BlofeldParam.new(\multiBank1, 2, nil, (0..7)), // these seem to be read-only
			BlofeldParam.new(\multiSound1, 3, nil, (0..127)),
			BlofeldParam.new(\multiBank2, 4, nil, (0..7)),
			BlofeldParam.new(\multiSound2, 5, nil, (0..127)),
			BlofeldParam.new(\multiBank3, 6, nil, (0..7)),
			BlofeldParam.new(\multiSound3, 7, nil, (0..127)),
			BlofeldParam.new(\multiBank4, 8, nil, (0..7)),
			BlofeldParam.new(\multiSound4, 9, nil, (0..127)),
			BlofeldParam.new(\multiBank5, 10, nil, (0..7)),
			BlofeldParam.new(\multiSound5, 11, nil, (0..127)),
			BlofeldParam.new(\multiBank6, 12, nil, (0..7)),
			BlofeldParam.new(\multiSound6, 13, nil, (0..127)),
			BlofeldParam.new(\multiBank7, 14, nil, (0..7)),
			BlofeldParam.new(\multiSound7, 15, nil, (0..127)),
			BlofeldParam.new(\multiBank8, 16, nil, (0..7)),
			BlofeldParam.new(\multiSound8, 17, nil, (0..127)),
			BlofeldParam.new(\multiBank9, 18, nil, (0..7)),
			BlofeldParam.new(\multiSound9, 19, nil, (0..127)),
			BlofeldParam.new(\multiBank10, 20, nil, (0..7)),
			BlofeldParam.new(\multiSound10, 21, nil, (0..127)),
			BlofeldParam.new(\multiBank11, 22, nil, (0..7)),
			BlofeldParam.new(\multiSound11, 23, nil, (0..127)),
			BlofeldParam.new(\multiBank12, 24, nil, (0..7)),
			BlofeldParam.new(\multiSound12, 25, nil, (0..127)),
			BlofeldParam.new(\multiBank13, 26, nil, (0..7)),
			BlofeldParam.new(\multiSound13, 27, nil, (0..127)),
			BlofeldParam.new(\multiBank14, 28, nil, (0..7)),
			BlofeldParam.new(\multiSound14, 29, nil, (0..127)),
			BlofeldParam.new(\multiBank15, 30, nil, (0..7)),
			BlofeldParam.new(\multiSound15, 31, nil, (0..127)),
			BlofeldParam.new(\multiBank16, 32, nil, (0..7)),
			BlofeldParam.new(\multiSound16, 33, nil, (0..127)),
			BlofeldParam.new(\autoEdit, 35, nil, (0..1)),
			BlofeldParam.new(\midiChannel, 36, nil, (0..16)), // omni..16
			BlofeldParam.new(\deviceID, 37, nil, (0..126)),
			BlofeldParam.new(\popupTime, 38, nil, (1..127)), // 0.1s..15.5s
			BlofeldParam.new(\contrast, 39, nil, (0..127)),
			BlofeldParam.new(\masterTune, 40, nil, (54..74)), // 430..450
			BlofeldParam.new(\transpose, 41, nil, (52..76)), // -12..+12
			BlofeldParam.new(\ctrlSend, 44, nil, (0..3)),
			BlofeldParam.new(\ctrlReceive, 45, nil, (0..1)),
			BlofeldParam.new(\clock, 48, nil, (0..1)), // auto, internal
			BlofeldParam.new(\velCurve, 50, nil, (0..8)), // linear..fix
			BlofeldParam.new(\controlW, 51, nil, (0..120)),
			BlofeldParam.new(\controlX, 52, nil, (0..120)),
			BlofeldParam.new(\controlY, 53, nil, (0..120)),
			BlofeldParam.new(\controlZ, 54, nil, (0..120)),
			BlofeldParam.new(\volume, 55, 7, (0..127)),
			BlofeldParam.new(\catFilter, 56, nil, Blofeld.catFilter),
		];

		BlofeldParam.initGroup(this);
	}
}
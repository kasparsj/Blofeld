BlofeldControlParams {
	classvar <>params;
	classvar <>byName;
	classvar <>byControl;

	*initClass {
		params = [
			// control only
			BlofeldParam.new(\bankMSB, nil, 0, (0..127)), //
			// pan does not work
			// to change panning in multimode, whole multidump needs to be uploaded
			BlofeldParam.new(\pan, nil, 10, (0..127)),
			BlofeldParam.new(\expression, nil, 11, (0..127)),
			BlofeldParam.new(\bankLSB, nil, 32, Blofeld.bank), // (a...h)
			BlofeldParam.new(\pitchmod, nil, 50, (0..127)),
			BlofeldParam.new(\sustainPedal, nil, 64, (0..127)),
			BlofeldParam.new(\sustenuto, nil, 66, (0..127)),
			BlofeldParam.new(\allSoundOff, nil, 120, [0]),
			BlofeldParam.new(\resetAllControllers, nil, 121, [0]),
			BlofeldParam.new(\localControl, nil, 122, (0..127)),
			BlofeldParam.new(\allNotesOff, nil, 123, [0]),
			// does not seem to be working:
			// BlofeldParam.new(\omniModeOff, nil, 124, [0]),
			// BlofeldParam.new(\omniModeOn, nil, 125, [0]),
			// BlofeldParam.new(\polyModeOff, nil, 126, [0]), // 0
			// BlofeldParam.new(\polyModeOn, nil, 127, [0]),
		];

		byName = ();
		byControl = ();

		[BlofeldGlobal, BlofeldSound, BlofeldMulti].do { |paramsClass|
			Class.initClassTree(paramsClass);
			paramsClass.params.do { |param|
				if (param.control != nil, {
					byName.put(param.name, param);
					byControl.put(param.control, param);
				});
			};
		};

		params.do { |param|
			byName.put(param.name, param);
			byControl.put(param.control, param);
		};
	}
}
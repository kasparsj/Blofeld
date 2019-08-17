BlofeldParam {
	classvar <params;
	classvar <byName;
	classvar <paramSysex;
	classvar <globalSysex;
	classvar <byControl;
	classvar <groups;

	var <name;
	var <sysex;
	var <control;
	var <values;
	var <isGlobal = false;

	*initClass {
		params = [
			// osc1
			BlofeldParam.new(\osc1Octave, 1, 27, (0..8)*12+16),
			BlofeldParam.new(\osc1Semitone, 2, 28, (52..76)),
			BlofeldParam.new(\osc1Detune, 3, 29, (0..127)),
			BlofeldParam.new(\osc1BendRange, 4, nil, (40..88)),
			BlofeldParam.new(\osc1Keytrack, 5, nil, (0..127)),
			BlofeldParam.new(\osc1FMSource, 6, nil, (0..11)),
			BlofeldParam.new(\osc1FM, 7, 30, (0..127)),
			BlofeldParam.new(\osc1Shape, 8, 31, (0..72)),
			BlofeldParam.new(\osc1PW, 9, 33, (0..127)),
			BlofeldParam.new(\osc1PWMSource, 10, nil, (0..30)),
			BlofeldParam.new(\osc1PWMAmount, 11, 34, (0..127)),
			BlofeldParam.new(\osc1LimitWT, 14, nil, (0..1)),
			BlofeldParam.new(\osc1Brilliance, 16, nil, (0..127)), // 12
			// osc2
			BlofeldParam.new(\osc2Octave, 17, 35, (0..8)*12+16),
			BlofeldParam.new(\osc2Semitone, 18, 36, (52..76)),
			BlofeldParam.new(\osc2Detune, 19, 37, (0..127)),
			BlofeldParam.new(\osc2BendRange, 20, nil, (40..88)),
			BlofeldParam.new(\osc2Keytrack, 21, nil, (0..127)),
			BlofeldParam.new(\osc2FMSource, 22, nil, (0..11)),
			BlofeldParam.new(\osc2FM, 23, 38, (0..127)),
			BlofeldParam.new(\osc2Shape, 24, 39, (0..72)),
			BlofeldParam.new(\osc2PW, 25, 40, (0..127)),
			BlofeldParam.new(\osc2PWMSource, 26, nil, (0..30)),
			BlofeldParam.new(\osc2PWMAmount, 27, 41, (0..127)),
			BlofeldParam.new(\osc2LimitWT, 30, nil, (0..1)),
			BlofeldParam.new(\osc2Brilliance, 32, nil, (0..127)), // 25
			// osc3
			BlofeldParam.new(\osc3Octave, 33, 42, (0..8)*12+16),
			BlofeldParam.new(\osc3Semitone, 34, 43, (52..76)),
			BlofeldParam.new(\osc3Detune, 35, 44, (0..127)),
			BlofeldParam.new(\osc3BendRange, 36, nil, (40..88)),
			BlofeldParam.new(\osc3Keytrack, 37, nil, (0..127)),
			BlofeldParam.new(\osc3FMSource, 38, nil, (0..11)),
			BlofeldParam.new(\osc3FM, 39, 45, (0..127)),
			BlofeldParam.new(\osc3Shape, 40, 46, (0..4)), // only supports basic shapes
			BlofeldParam.new(\osc3PW, 41, 47, (0..127)),
			BlofeldParam.new(\osc3PWMSource, 42, nil, (0..30)),
			BlofeldParam.new(\osc3PWMAmount, 43, 48, (0..127)),
			BlofeldParam.new(\osc3Brilliance, 48, nil, (0..127)), // 37
			// osc settings
			BlofeldParam.new(\osc2SyncO3, 49, 49, (0..1)), // Sync
			BlofeldParam.new(\oscPitchSource, 50, nil, (0..30)),
			BlofeldParam.new(\oscPitchAmount, 51, nil, (0..127)),
			// glide
			BlofeldParam.new(\glide, 53, 65, (0..1)), // 41
			BlofeldParam.new(\glideMode, 56, 51, (0..3)),
			BlofeldParam.new(\glideRate, 57, nil, (0..127)),
			// unisono
			BlofeldParam.new(\allocationUnisonoMode, 58, nil, (0..127)), // 44
			BlofeldParam.new(\unisonoUniDetune, 59, nil, (0..127)),
			// mixer
			BlofeldParam.new(\osc1Level, 61, 52, (0..127)), // 46
			BlofeldParam.new(\osc1Balance, 62, 53, (0..127)),
			BlofeldParam.new(\osc2Level, 63, 56, (0..127)),
			BlofeldParam.new(\osc2Balance, 64, 57, (0..127)),
			BlofeldParam.new(\osc3Level, 65, 58, (0..127)),
			BlofeldParam.new(\osc3Balance, 66, 59, (0..127)),
			BlofeldParam.new(\noiseLevel, 67, 60, (0..127)),
			BlofeldParam.new(\noiseBalance, 68, 61, (0..127)),
			BlofeldParam.new(\noiseColour, 69, 62, (0..127)),
			BlofeldParam.new(\ringModLevel, 71, 54, (0..127)),
			BlofeldParam.new(\ringModBalance, 72, 55, (0..127)),
			// filter1
			BlofeldParam.new(\filter1Type, 77, 68, (0..11)), // 57
			BlofeldParam.new(\filter1Cutoff, 78, 69, (0..127)),
			BlofeldParam.new(\filter1Resonance, 80, 70, (0..127)),
			BlofeldParam.new(\filter1Drive, 81, 71, (0..127)),
			BlofeldParam.new(\filter1DriveCurve, 82, nil, (0..12)),
			BlofeldParam.new(\filter1Keytrack, 86, 72, (0..127)),
			BlofeldParam.new(\filter1EnvAmount, 87, 73, (0..127)),
			BlofeldParam.new(\filter1EnvVelocity, 88, 74, (0..127)),
			BlofeldParam.new(\filter1ModSource, 89, nil, (0..30)),
			BlofeldParam.new(\filter1ModAmount, 90, 75, (0..127)), // cutoffMod
			BlofeldParam.new(\filter1FMSource, 91, nil, (0..11)),
			BlofeldParam.new(\filter1FMAmount, 92, 76, (0..127)),
			BlofeldParam.new(\filter1Pan, 93, 77, (0..127)),
			BlofeldParam.new(\filter1PanSource, 94, nil, (0..30)),
			BlofeldParam.new(\filter1PanAmount, 95, 78, (0..127)), // panMod
			// filter2
			BlofeldParam.new(\filter2Type, 97, 79, (0..11)), // 72
			BlofeldParam.new(\filter2Cutoff, 98, 80, (0..127)),
			BlofeldParam.new(\filter2Resonance, 100, 81, (0..127)),
			BlofeldParam.new(\filter2Drive, 101, 82, (0..127)),
			BlofeldParam.new(\filter2DriveCurve, 102, nil, (0..12)),
			BlofeldParam.new(\filter2Keytrack, 106, 83, (0..127)),
			BlofeldParam.new(\filter2EnvAmount, 107, 84, (0..127)),
			BlofeldParam.new(\filter2EnvVelocity, 108, 85, (0..127)),
			BlofeldParam.new(\filter2ModSource, 109, nil, (0..30)),
			BlofeldParam.new(\filter2ModAmount, 100, 86, (0..127)), // cutoffMod
			BlofeldParam.new(\filter2FMSource, 111, nil, (0..11)),
			BlofeldParam.new(\filter2FMAmount, 112, 87, (0..127)),
			BlofeldParam.new(\filter2Pan, 113, 88, (0..127)),
			BlofeldParam.new(\filter2PanSource, 114, nil, (0..30)),
			BlofeldParam.new(\filter2PanAmount, 115, 89, (0..127)), // panMod
			// filter settings
			BlofeldParam.new(\filterRouting, 117, 67, (0..1)), // 87
			// amplifier
			BlofeldParam.new(\ampVolume, 121, 90, (0..127)), // 88
			BlofeldParam.new(\ampVelocity, 122, 91, (0..127)),
			BlofeldParam.new(\ampModSource, 123, nil, (0..30)),
			BlofeldParam.new(\ampModAmount, 124, 92, (0..127)),
			// effect1
			BlofeldParam.new(\effect1Type, 128, nil, (0..127)),
			BlofeldParam.new(\effect1Mix, 129, 93, (0..127)),
			BlofeldParam.new(\effect1Param1, 130, nil, (0..127)),
			BlofeldParam.new(\effect1Param2, 131, nil, (0..127)),
			BlofeldParam.new(\effect1Param3, 132, nil, (0..127)),
			BlofeldParam.new(\effect1Param4, 133, nil, (0..127)),
			BlofeldParam.new(\effect1Param5, 134, nil, (0..127)),
			BlofeldParam.new(\effect1Param6, 135, nil, (0..127)),
			BlofeldParam.new(\effect1Param7, 136, nil, (0..127)),
			BlofeldParam.new(\effect1Param8, 137, nil, (0..127)),
			BlofeldParam.new(\effect1Param9, 138, nil, (0..127)),
			BlofeldParam.new(\effect1Param10, 139, nil, (0..127)),
			BlofeldParam.new(\effect1Param11, 140, nil, (0..127)),
			BlofeldParam.new(\effect1Param12, 141, nil, (0..127)),
			BlofeldParam.new(\effect1Param13, 142, nil, (0..127)),
			BlofeldParam.new(\effect1Param14, 143, nil, (0..127)),
			// effect2
			BlofeldParam.new(\effect2Type, 144, nil, (0..127)),
			BlofeldParam.new(\effect2Mix, 145, 94, (0..127)),
			BlofeldParam.new(\effect2Param1, 146, nil, (0..127)),
			BlofeldParam.new(\effect2Param2, 147, nil, (0..127)),
			BlofeldParam.new(\effect2Param3, 148, nil, (0..127)),
			BlofeldParam.new(\effect2Param4, 149, nil, (0..127)),
			BlofeldParam.new(\effect2Param5, 150, nil, (0..127)),
			BlofeldParam.new(\effect2Param6, 151, nil, (0..127)),
			BlofeldParam.new(\effect2Param7, 152, nil, (0..127)),
			BlofeldParam.new(\effect2Param8, 153, nil, (0..127)),
			BlofeldParam.new(\effect2Param9, 154, nil, (0..127)),
			BlofeldParam.new(\effect2Param10, 155, nil, (0..127)),
			BlofeldParam.new(\effect2Param11, 156, nil, (0..127)),
			BlofeldParam.new(\effect2Param12, 157, nil, (0..127)),
			BlofeldParam.new(\effect2Param13, 158, nil, (0..127)),
			BlofeldParam.new(\effect2Param14, 159, nil, (0..127)),
			// lfo1
			BlofeldParam.new(\lfo1Shape, 160, 15, (0..5)),
			BlofeldParam.new(\lfo1Speed, 161, 16, (0..127)),
			BlofeldParam.new(\lfo1Sync, 163, 17, (0..1)),
			BlofeldParam.new(\lfo1Clocked, 164, nil, (0..1)),
			BlofeldParam.new(\lfo1StartPhase, 165, nil, (0..127)),
			BlofeldParam.new(\lfo1Delay, 166, 18, (0..127)),
			BlofeldParam.new(\lfo1Fade, 167, nil, (0..127)),
			BlofeldParam.new(\lfo1Keytrack, 170, nil, (0..127)),
			// lfo2
			BlofeldParam.new(\lfo2Shape, 172, 19, (0..5)),
			BlofeldParam.new(\lfo2Speed, 173, 20, (0..127)),
			BlofeldParam.new(\lfo2Sync, 175, 21, (0..1)),
			BlofeldParam.new(\lfo2Clocked, 176, nil, (0..1)),
			BlofeldParam.new(\lfo2StartPhase, 177, nil, (0..127)),
			BlofeldParam.new(\lfo2Delay, 178, 22, (0..127)),
			BlofeldParam.new(\lfo2Fade, 179, nil, (0..127)),
			BlofeldParam.new(\lfo2Keytrack, 182, nil, (0..127)),
			// lfo3
			BlofeldParam.new(\lfo3Shape, 184, 23, (0..5)),
			BlofeldParam.new(\lfo3Speed, 185, 24, (0..127)),
			BlofeldParam.new(\lfo3Sync, 187, 25, (0..1)),
			BlofeldParam.new(\lfo3Clocked, 188, nil, (0..1)),
			BlofeldParam.new(\lfo3StartPhase, 189, nil, (0..127)),
			BlofeldParam.new(\lfo3Delay, 190, 26, (0..127)),
			BlofeldParam.new(\lfo3Fade, 191, nil, (0..127)),
			BlofeldParam.new(\lfo3Keytrack, 194, nil, (0..127)),
			// filter env
			BlofeldParam.new(\filterEnvMode, 196, nil, (0..4)),
			BlofeldParam.new(\filterEnvAttack, 199, 95, (0..127)),
			BlofeldParam.new(\filterEnvAttackLevel, 200, nil, (0..127)),
			BlofeldParam.new(\filterEnvDecay, 201, 96, (0..127)),
			BlofeldParam.new(\filterEnvSustain, 202, 97, (0..127)),
			BlofeldParam.new(\filterEnvDecay2, 203, 98, (0..127)),
			BlofeldParam.new(\filterEnvSustain2, 204, 99, (0..127)),
			BlofeldParam.new(\filterEnvRelease, 205, 100, (0..127)),
			// amplifier env
			BlofeldParam.new(\ampEnvMode, 208, nil, (0..4)),
			BlofeldParam.new(\ampEnvAttack, 211, 101, (0..127)),
			BlofeldParam.new(\ampEnvAttackLevel, 212, nil, (0..127)),
			BlofeldParam.new(\ampEnvDecay, 213, 102, (0..127)),
			BlofeldParam.new(\ampEnvSustain, 214, 103, (0..127)),
			BlofeldParam.new(\ampEnvDecay2, 215, 104, (0..127)),
			BlofeldParam.new(\ampEnvSustain2, 216, 105, (0..127)),
			BlofeldParam.new(\ampEnvRelease, 217, 106, (0..127)),
			// env3
			BlofeldParam.new(\env3Mode, 220, nil, (0..4)),
			BlofeldParam.new(\env3Attack, 223, 107, (0..127)),
			BlofeldParam.new(\env3AttackLevel, 224, nil, (0..127)),
			BlofeldParam.new(\env3Decay, 225, 108, (0..127)),
			BlofeldParam.new(\env3Sustain, 226, 109, (0..127)),
			BlofeldParam.new(\env3Decay2, 227, 110, (0..127)),
			BlofeldParam.new(\env3Sustain2, 228, 111, (0..127)),
			BlofeldParam.new(\env3Release, 229, 112, (0..127)),
			// env4
			BlofeldParam.new(\env4Mode, 232, nil, (0..4)),
			BlofeldParam.new(\env4Attack, 235, 113, (0..127)),
			BlofeldParam.new(\env4AttackLevel, 236, nil, (0..127)),
			BlofeldParam.new(\env4Decay, 237, 114, (0..127)),
			BlofeldParam.new(\env4Sustain, 238, 115, (0..127)),
			BlofeldParam.new(\env4Decay2, 239, 116, (0..127)),
			BlofeldParam.new(\env4Sustain2, 240, 117, (0..127)),
			BlofeldParam.new(\env4Release, 241, 118, (0..127)),
			// modifier1
			BlofeldParam.new(\modifier1SourceA, 245, nil, (0..30)),
			BlofeldParam.new(\modifier1SourceB, 246, nil, (0..30)),
			BlofeldParam.new(\modifier1Operation, 247, nil, (0..7)),
			BlofeldParam.new(\modifier1Constant, 248, nil, (0..127)),
			// modifier2
			BlofeldParam.new(\modifier2SourceA, 249, nil, (0..30)),
			BlofeldParam.new(\modifier2SourceB, 250, nil, (0..30)),
			BlofeldParam.new(\modifier2Operation, 251, nil, (0..7)),
			BlofeldParam.new(\modifier2Constant, 252, nil, (0..127)),
			// modifier3
			BlofeldParam.new(\modifier3SourceA, 253, nil, (0..30)),
			BlofeldParam.new(\modifier3SourceB, 254, nil, (0..30)),
			BlofeldParam.new(\modifier3Operation, 255, nil, (0..7)),
			BlofeldParam.new(\modifier3Constant, 256, nil, (0..127)),
			// modifier4
			BlofeldParam.new(\modifier4SourceA, 257, nil, (0..30)),
			BlofeldParam.new(\modifier4SourceB, 258, nil, (0..30)),
			BlofeldParam.new(\modifier4Operation, 259, nil, (0..7)),
			BlofeldParam.new(\modifier4Constant, 260, nil, (0..127)),
			// modulations
			BlofeldParam.new(\modulation1Src, 261, nil, (0..30)),
			BlofeldParam.new(\modulation1Dst, 262, nil, (0..53)),
			BlofeldParam.new(\modulation1Amount, 263, nil, (0..127)),
			BlofeldParam.new(\modulation2Src, 264, nil, (0..30)),
			BlofeldParam.new(\modulation2Dst, 265, nil, (0..53)),
			BlofeldParam.new(\modulation2Amount, 266, nil, (0..127)),
			BlofeldParam.new(\modulation3Src, 267, nil, (0..30)),
			BlofeldParam.new(\modulation3Dst, 268, nil, (0..53)),
			BlofeldParam.new(\modulation3Amount, 269, nil, (0..127)),
			BlofeldParam.new(\modulation4Src, 270, nil, (0..30)),
			BlofeldParam.new(\modulation4Dst, 271, nil, (0..53)),
			BlofeldParam.new(\modulation4Amount, 272, nil, (0..127)),
			BlofeldParam.new(\modulation5Src, 273, nil, (0..30)),
			BlofeldParam.new(\modulation5Dst, 274, nil, (0..53)),
			BlofeldParam.new(\modulation5Amount, 275, nil, (0..127)),
			// arp
			BlofeldParam.new(\arpMode, 311, 14, (0..3)),
			BlofeldParam.new(\arpPattern, 312, nil, (0..16)),
			BlofeldParam.new(\arpClock, 314, nil, (0..42)),
			BlofeldParam.new(\arpLength, 315, 13, (0..43)),
			BlofeldParam.new(\arpOctave, 316, 12, (0..9)),

			// global data
			BlofeldGlobal.new(\multiMode, 1, nil, (0..1)),
			BlofeldGlobal.new(\multiBank1, 2, nil, (0..7)), // these seem to be read-only
			BlofeldGlobal.new(\multiSound1, 3, nil, (0..127)),
			BlofeldGlobal.new(\multiBank2, 4, nil, (0..7)),
			BlofeldGlobal.new(\multiSound2, 5, nil, (0..127)),
			BlofeldGlobal.new(\multiBank3, 6, nil, (0..7)),
			BlofeldGlobal.new(\multiSound3, 7, nil, (0..127)),
			BlofeldGlobal.new(\multiBank4, 8, nil, (0..7)),
			BlofeldGlobal.new(\multiSound4, 9, nil, (0..127)),
			BlofeldGlobal.new(\multiBank5, 10, nil, (0..7)),
			BlofeldGlobal.new(\multiSound5, 11, nil, (0..127)),
			BlofeldGlobal.new(\multiBank6, 12, nil, (0..7)),
			BlofeldGlobal.new(\multiSound6, 13, nil, (0..127)),
			BlofeldGlobal.new(\multiBank7, 14, nil, (0..7)),
			BlofeldGlobal.new(\multiSound7, 15, nil, (0..127)),
			BlofeldGlobal.new(\multiBank8, 16, nil, (0..7)),
			BlofeldGlobal.new(\multiSound8, 17, nil, (0..127)),
			BlofeldGlobal.new(\multiBank9, 18, nil, (0..7)),
			BlofeldGlobal.new(\multiSound9, 19, nil, (0..127)),
			BlofeldGlobal.new(\multiBank10, 20, nil, (0..7)),
			BlofeldGlobal.new(\multiSound10, 21, nil, (0..127)),
			BlofeldGlobal.new(\multiBank11, 22, nil, (0..7)),
			BlofeldGlobal.new(\multiSound11, 23, nil, (0..127)),
			BlofeldGlobal.new(\multiBank12, 24, nil, (0..7)),
			BlofeldGlobal.new(\multiSound12, 25, nil, (0..127)),
			BlofeldGlobal.new(\multiBank13, 26, nil, (0..7)),
			BlofeldGlobal.new(\multiSound13, 27, nil, (0..127)),
			BlofeldGlobal.new(\multiBank14, 28, nil, (0..7)),
			BlofeldGlobal.new(\multiSound14, 29, nil, (0..127)),
			BlofeldGlobal.new(\multiBank15, 30, nil, (0..7)),
			BlofeldGlobal.new(\multiSound15, 31, nil, (0..127)),
			BlofeldGlobal.new(\multiBank16, 32, nil, (0..7)),
			BlofeldGlobal.new(\multiSound16, 33, nil, (0..127)),
			BlofeldGlobal.new(\autoEdit, 35, nil, (0..1)),
			BlofeldGlobal.new(\midiChannel, 36, nil, (0..16)), // omni..16
			BlofeldGlobal.new(\deviceID, 37, nil, (0..126)),
			BlofeldGlobal.new(\popupTime, 38, nil, (1..127)), // 0.1s..15.5s
			BlofeldGlobal.new(\contrast, 39, nil, (0..127)),
			BlofeldGlobal.new(\masterTune, 40, nil, (54..74)), // 430..450
			BlofeldGlobal.new(\transpose, 41, nil, (52..76)), // -12..+12
			BlofeldGlobal.new(\ctrlSend, 44, nil, (0..3)),
			BlofeldGlobal.new(\ctrlReceive, 45, nil, (0..1)),
			BlofeldGlobal.new(\clock, 48, nil, (0..1)), // auto, internal
			BlofeldGlobal.new(\velCurve, 50, nil, (0..8)), // linear..fix
			BlofeldGlobal.new(\controlW, 51, nil, (0..120)),
			BlofeldGlobal.new(\controlX, 52, nil, (0..120)),
			BlofeldGlobal.new(\controlY, 53, nil, (0..120)),
			BlofeldGlobal.new(\controlZ, 54, nil, (0..120)),
			BlofeldGlobal.new(\volume, 55, 7, (0..127)),
			BlofeldGlobal.new(\catFilter, 56, nil, (0..13)),

			// control only
			BlofeldParam.new(\bankMSB, nil, 0, (0..127)), //
			BlofeldParam.new(\pan, nil, 10, (0..127)),
			BlofeldParam.new(\expression, nil, 11, (0..127)),
			BlofeldParam.new(\bankLSB, nil, 32, (0..7)), // (a...h)
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
		this.initGroups();
	}

	*initGroups {
		byName = ();
		paramSysex = ();
		globalSysex = ();
		byControl = ();
		params.do({ |value|
			byName.put(value.name, value);
			if (value.sysex != nil, {
				if (value.isGlobal, {
					globalSysex.put(value.sysex, value);
				}, {
					paramSysex.put(value.sysex, value);
				});
			});
			if (value.control != nil, {
				byControl.put(value.control, value);
			});
		});
		groups = (
			\sysex: paramSysex.values ++ globalSysex.values,
			\control: byControl.values,
			\osc1: params[0..12],
			\osc2: params[13..25],
			\osc3: params[26..37],
			\filter1: params[57..71],
			\filter2: params[72..86],
			\amp: params[88..91],
		);
	}

	*new { |name, sysex, control, values|
		^super.newCopyArgs(name, sysex, control, values);
	}

	rand {
		^values.choose;
	}

	asString {
		^name;
	}
}

BlofeldGlobal : BlofeldParam {
	*new { |name, sysex, control, values|
		^super.newCopyArgs(name, sysex, control, values, true);
	}
}
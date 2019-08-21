BlofeldParam {
	classvar <params;
	classvar <multiParams;
	classvar <globalParams;

	classvar <byName;
	classvar <multiByName;
	classvar <globalByName;
	classvar <paramSysex;
	classvar <multiSysex;
	classvar <globalSysex;
	classvar <byControl;
	classvar <groups;

	var <name;
	var <sysex;
	var <control;
	var <values;
	var <defaultValue;
	var <relParam;
	var <>sequential = true;

	*initClass {
		Class.initClassTree(Blofeld);

		params = [
			// osc1
			BlofeldParam.new(\osc1Octave, 1, 27, Blofeld.octave, 64).sequential_(false), // 16..112
			BlofeldParam.new(\osc1Semitone, 2, 28, (52..76), 64),
			BlofeldParam.new(\osc1Detune, 3, 29, Blofeld.m64p63, 64),
			BlofeldParam.new(\osc1BendRange, 4, nil, Blofeld.m24p24, 64),
			BlofeldParam.new(\osc1Keytrack, 5, nil, Blofeld.m200p196perc, 64),
			BlofeldParam.new(\osc1FMSource, 6, nil, Blofeld.fmSource, 0, \osc1FMAmount),
			BlofeldParam.new(\osc3FMAmount, 7, 30, (0..127)),
			BlofeldParam.new(\osc1Shape, 8, 31, Blofeld.shape),
			BlofeldParam.new(\osc1Pulsewidth, 9, 33, (0..127)),
			BlofeldParam.new(\osc1PWMSource, 10, nil, Blofeld.modSource, 0, \osc1PWMAmount),
			BlofeldParam.new(\osc1PWMAmount, 11, 34, Blofeld.m64p63, 64),
			BlofeldParam.new(\osc1LimitWT, 14, nil, Blofeld.onOff),
			BlofeldParam.new(\osc1Brilliance, 16, nil, (0..127)), // 12
			// osc2
			BlofeldParam.new(\osc2Octave, 17, 35, Blofeld.octave, 64).sequential_(false),
			BlofeldParam.new(\osc2Semitone, 18, 36, (52..76), 64),
			BlofeldParam.new(\osc2Detune, 19, 37, Blofeld.m64p63, 64),
			BlofeldParam.new(\osc2BendRange, 20, nil, Blofeld.m24p24, 64),
			BlofeldParam.new(\osc2Keytrack, 21, nil, Blofeld.m200p196perc, 64),
			BlofeldParam.new(\osc2FMSource, 22, nil, Blofeld.fmSource, 0, \osc2FMAmount),
			BlofeldParam.new(\osc3FMAmount, 23, 38, (0..127)),
			BlofeldParam.new(\osc2Shape, 24, 39, Blofeld.shape),
			BlofeldParam.new(\osc2Pulsewidth, 25, 40, (0..127)),
			BlofeldParam.new(\osc2PWMSource, 26, nil, Blofeld.modSource, 0, \osc2PWMAmount),
			BlofeldParam.new(\osc2PWMAmount, 27, 41, Blofeld.m64p63, 64),
			BlofeldParam.new(\osc2LimitWT, 30, nil, Blofeld.onOff),
			BlofeldParam.new(\osc2Brilliance, 32, nil, (0..127)),
			// osc3
			BlofeldParam.new(\osc3Octave, 33, 42, Blofeld.octave, 64).sequential_(false),
			BlofeldParam.new(\osc3Semitone, 34, 43, (52..76), 64),
			BlofeldParam.new(\osc3Detune, 35, 44, Blofeld.m64p63, 64),
			BlofeldParam.new(\osc3BendRange, 36, nil, Blofeld.m24p24, 64),
			BlofeldParam.new(\osc3Keytrack, 37, nil, Blofeld.m200p196perc, 64),
			BlofeldParam.new(\osc3FMSource, 38, nil, Blofeld.fmSource, 0, \osc3FMAmount),
			BlofeldParam.new(\osc3FMAmount, 39, 45, (0..127)),
			BlofeldParam.new(\osc3Shape, 40, 46, Blofeld.osc3Shape),
			BlofeldParam.new(\osc3Pulsewidth, 41, 47, (0..127)),
			BlofeldParam.new(\osc3PWMSource, 42, nil, Blofeld.modSource, 0, \osc3PWMAmount),
			BlofeldParam.new(\osc3PWMAmount, 43, 48, Blofeld.m64p63, 64),
			BlofeldParam.new(\osc3Brilliance, 48, nil, (0..127)),
			// osc settings
			BlofeldParam.new(\osc2SyncO3, 49, 49, Blofeld.onOff),
			BlofeldParam.new(\oscPitchSource, 50, nil, Blofeld.modSource, 0, \oscPitchAmount),
			BlofeldParam.new(\oscPitchAmount, 51, nil, Blofeld.m64p63, 64),
			// glide
			BlofeldParam.new(\glide, 53, 65, Blofeld.onOff),
			BlofeldParam.new(\glideMode, 56, 51, Blofeld.glideMode, 0, \glide),
			BlofeldParam.new(\glideRate, 57, nil, (0..127), 0, \glide),
			// unisono
			BlofeldParam.new(\allocationUnisonoMode, 58, nil, (0..127)),
			BlofeldParam.new(\unisonoUniDetune, 59, nil, (0..127)),
			// mixer
			BlofeldParam.new(\osc1Level, 61, 52, (0..127), 127), // 46
			BlofeldParam.new(\osc1Balance, 62, 53, Blofeld.fBalance),
			BlofeldParam.new(\osc2Level, 63, 56, (0..127), 127),
			BlofeldParam.new(\osc2Balance, 64, 57, Blofeld.fBalance),
			BlofeldParam.new(\osc3Level, 65, 58, (0..127), 127),
			BlofeldParam.new(\osc3Balance, 66, 59, Blofeld.fBalance),
			BlofeldParam.new(\noiseLevel, 67, 60, (0..127)),
			BlofeldParam.new(\noiseBalance, 68, 61, Blofeld.fBalance, 0, \noiseLevel),
			BlofeldParam.new(\noiseColour, 69, 62, Blofeld.m64p63, 64, \noiseLevel),
			BlofeldParam.new(\ringModLevel, 71, 54, (0..127)),
			BlofeldParam.new(\ringModBalance, 72, 55, Blofeld.fBalance, 0, \ringModLevel),
			// filter1
			BlofeldParam.new(\filter1Type, 77, 68, Blofeld.filterType), // 57
			BlofeldParam.new(\filter1Cutoff, 78, 69, (0..127), 127),
			BlofeldParam.new(\filter1Resonance, 80, 70, (0..127)),
			BlofeldParam.new(\filter1Drive, 81, 71, (0..127)),
			BlofeldParam.new(\filter1DriveCurve, 82, nil, Blofeld.driveCurve),
			BlofeldParam.new(\filter1Keytrack, 86, 72, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter1EnvAmount, 87, 73, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter1EnvVelocity, 88, 74, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter1ModSource, 89, nil, Blofeld.modSource, 0, \filter1ModAmount),
			BlofeldParam.new(\filter1ModAmount, 90, 75, Blofeld.m64p63, 64), // cutoffMod
			BlofeldParam.new(\filter1FMSource, 91, nil, Blofeld.fmSource, 0, \filter1FMAmount),
			BlofeldParam.new(\filter1FMAmount, 92, 76, (0..127)),
			BlofeldParam.new(\filter1Pan, 93, 77, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter1PanSource, 94, nil, Blofeld.modSource, 0, \filter1PanAmount),
			BlofeldParam.new(\filter1PanAmount, 95, 78, Blofeld.m64p63, 64), // panMod
			// filter2
			BlofeldParam.new(\filter2Type, 97, 79, Blofeld.filterType), // 72
			BlofeldParam.new(\filter2Cutoff, 98, 80, (0..127), 127),
			BlofeldParam.new(\filter2Resonance, 100, 81, (0..127)),
			BlofeldParam.new(\filter2Drive, 101, 82, (0..127)),
			BlofeldParam.new(\filter2DriveCurve, 102, nil, Blofeld.driveCurve),
			BlofeldParam.new(\filter2Keytrack, 106, 83, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter2EnvAmount, 107, 84, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter2EnvVelocity, 108, 85, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter2ModSource, 109, nil, Blofeld.modSource, 0, \filter2ModAmount),
			BlofeldParam.new(\filter2ModAmount, 110, 86, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter2FMSource, 111, nil, Blofeld.fmSource, 0, \filter2FMAmount),
			BlofeldParam.new(\filter2FMAmount, 112, 87, (0..127)),
			BlofeldParam.new(\filter2Pan, 113, 88, Blofeld.m64p63, 64),
			BlofeldParam.new(\filter2PanSource, 114, nil, Blofeld.modSource, 0, \filter2PanAmount),
			BlofeldParam.new(\filter2PanAmount, 115, 89, Blofeld.m64p63, 64), // panMod
			// filter settings
			BlofeldParam.new(\filterRouting, 117, 67, Blofeld.filterRouting), // 87
			// amplifier
			BlofeldParam.new(\ampVolume, 121, 90, (0..127), -1), // 88
			BlofeldParam.new(\ampVelocity, 122, 91, Blofeld.m64p63, -1),
			BlofeldParam.new(\ampModSource, 123, nil, Blofeld.modSource, 0, \ampModAmount),
			BlofeldParam.new(\ampModAmount, 124, 92, Blofeld.m64p63, 64),
			// effect1
			BlofeldParam.new(\effect1Type, 128, nil, Blofeld.effect1Type, 0, \effect1Mix),
			BlofeldParam.new(\effect1Mix, 129, 93, (0..127)),
			BlofeldParam.new(\effect1Param1, 130, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param2, 131, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param3, 132, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param4, 133, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param5, 134, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param6, 135, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param7, 136, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param8, 137, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param9, 138, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param10, 139, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param11, 140, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param12, 141, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param13, 142, nil, (0..127), 0, \effect1Mix),
			BlofeldParam.new(\effect1Param14, 143, nil, (0..127), 0, \effect1Mix),
			// effect2
			BlofeldParam.new(\effect2Type, 144, nil, Blofeld.effect2Type, 0, \effect2Mix),
			BlofeldParam.new(\effect2Mix, 145, 94, (0..127)),
			BlofeldParam.new(\effect2Param1, 146, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param2, 147, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param3, 148, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param4, 149, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param5, 150, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param6, 151, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param7, 152, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param8, 153, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param9, 154, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param10, 155, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param11, 156, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param12, 157, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param13, 158, nil, (0..127), 0, \effect2Mix),
			BlofeldParam.new(\effect2Param14, 159, nil, (0..127), 0, \effect2Mix),
			// lfo1
			BlofeldParam.new(\lfo1Shape, 160, 15, Blofeld.lfoShape, -1),
			BlofeldParam.new(\lfo1Speed, 161, 16, (0..127)),
			BlofeldParam.new(\lfo1Sync, 163, 17, Blofeld.onOff),
			BlofeldParam.new(\lfo1Clocked, 164, nil, Blofeld.onOff),
			BlofeldParam.new(\lfo1StartPhase, 165, nil, (0..127)),
			BlofeldParam.new(\lfo1Delay, 166, 18, (0..127)),
			BlofeldParam.new(\lfo1Fade, 167, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\lfo1Keytrack, 170, nil, Blofeld.m200p196perc, 64),
			// lfo2
			BlofeldParam.new(\lfo2Shape, 172, 19, Blofeld.lfoShape, -1),
			BlofeldParam.new(\lfo2Speed, 173, 20, (0..127)),
			BlofeldParam.new(\lfo2Sync, 175, 21, Blofeld.onOff),
			BlofeldParam.new(\lfo2Clocked, 176, nil, Blofeld.onOff),
			BlofeldParam.new(\lfo2StartPhase, 177, nil, (0..127)),
			BlofeldParam.new(\lfo2Delay, 178, 22, (0..127)),
			BlofeldParam.new(\lfo2Fade, 179, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\lfo2Keytrack, 182, nil, Blofeld.m200p196perc, 64),
			// lfo3
			BlofeldParam.new(\lfo3Shape, 184, 23, Blofeld.lfoShape, -1),
			BlofeldParam.new(\lfo3Speed, 185, 24, (0..127)),
			BlofeldParam.new(\lfo3Sync, 187, 25, Blofeld.onOff),
			BlofeldParam.new(\lfo3Clocked, 188, nil, Blofeld.onOff),
			BlofeldParam.new(\lfo3StartPhase, 189, nil, (0..127)),
			BlofeldParam.new(\lfo3Delay, 190, 26, (0..127)),
			BlofeldParam.new(\lfo3Fade, 191, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\lfo3Keytrack, 194, nil, Blofeld.m200p196perc, 64),
			// filter env
			BlofeldParam.new(\filterEnvMode, 196, nil, Blofeld.envMode),
			BlofeldParam.new(\filterEnvAttack, 199, 95, (0..127), -1),
			BlofeldParam.new(\filterEnvAttackLevel, 200, nil, (0..127), 127),
			BlofeldParam.new(\filterEnvDecay, 201, 96, (0..127), -1),
			BlofeldParam.new(\filterEnvSustain, 202, 97, (0..127), -1),
			BlofeldParam.new(\filterEnvDecay2, 203, 98, (0..127), -1),
			BlofeldParam.new(\filterEnvSustain2, 204, 99, (0..127), -1),
			BlofeldParam.new(\filterEnvRelease, 205, 100, (0..127), -1),
			// amplifier env
			BlofeldParam.new(\ampEnvMode, 208, nil, Blofeld.envMode),
			BlofeldParam.new(\ampEnvAttack, 211, 101, (0..127), -1),
			BlofeldParam.new(\ampEnvAttackLevel, 212, nil, (0..127), 127),
			BlofeldParam.new(\ampEnvDecay, 213, 102, (0..127), -1),
			BlofeldParam.new(\ampEnvSustain, 214, 103, (0..127), -1),
			BlofeldParam.new(\ampEnvDecay2, 215, 104, (0..127), -1),
			BlofeldParam.new(\ampEnvSustain2, 216, 105, (0..127), -1),
			BlofeldParam.new(\ampEnvRelease, 217, 106, (0..127), -1),
			// env3
			BlofeldParam.new(\env3Mode, 220, nil, Blofeld.envMode),
			BlofeldParam.new(\env3Attack, 223, 107, (0..127), -1),
			BlofeldParam.new(\env3AttackLevel, 224, nil, (0..127), 127),
			BlofeldParam.new(\env3Decay, 225, 108, (0..127), -1),
			BlofeldParam.new(\env3Sustain, 226, 109, (0..127), -1),
			BlofeldParam.new(\env3Decay2, 227, 110, (0..127), -1),
			BlofeldParam.new(\env3Sustain2, 228, 111, (0..127), -1),
			BlofeldParam.new(\env3Release, 229, 112, (0..127), -1),
			// env4
			BlofeldParam.new(\env4Mode, 232, nil, Blofeld.envMode),
			BlofeldParam.new(\env4Attack, 235, 113, (0..127), -1),
			BlofeldParam.new(\env4AttackLevel, 236, nil, (0..127), 127),
			BlofeldParam.new(\env4Decay, 237, 114, (0..127), -1),
			BlofeldParam.new(\env4Sustain, 238, 115, (0..127), -1),
			BlofeldParam.new(\env4Decay2, 239, 116, (0..127), -1),
			BlofeldParam.new(\env4Sustain2, 240, 117, (0..127), -1),
			BlofeldParam.new(\env4Release, 241, 118, (0..127), -1),
			// modifier1
			BlofeldParam.new(\modifier1SourceA, 245, nil, Blofeld.modSource),
			BlofeldParam.new(\modifier1SourceB, 246, nil, Blofeld.modSourceB, -1, \modifier1SourceA),
			BlofeldParam.new(\modifier1Operation, 247, nil, Blofeld.operator, 0, \modifier1SourceA),
			BlofeldParam.new(\modifier1Constant, 248, nil, Blofeld.m64p63, 0, \modifier1SourceA),
			// modifier2
			BlofeldParam.new(\modifier2SourceA, 249, nil, Blofeld.modSource),
			BlofeldParam.new(\modifier2SourceB, 250, nil, Blofeld.modSourceB, -1, \modifier2SourceA),
			BlofeldParam.new(\modifier2Operation, 251, nil, Blofeld.operator, 0, \modifier2SourceA),
			BlofeldParam.new(\modifier2Constant, 252, nil, Blofeld.m64p63, 64, \modifier2SourceA),
			// modifier3
			BlofeldParam.new(\modifier3SourceA, 253, nil, Blofeld.modSource),
			BlofeldParam.new(\modifier3SourceB, 254, nil, Blofeld.modSourceB, -1, \modifier3SourceA),
			BlofeldParam.new(\modifier3Operation, 255, nil, Blofeld.operator, 0, \modifier3SourceA),
			BlofeldParam.new(\modifier3Constant, 256, nil, Blofeld.m64p63, 64, \modifier3SourceA),
			// modifier4
			BlofeldParam.new(\modifier4SourceA, 257, nil, Blofeld.modSource),
			BlofeldParam.new(\modifier4SourceB, 258, nil, Blofeld.modSourceB, -1, \modifier4SourceA),
			BlofeldParam.new(\modifier4Operation, 259, nil, Blofeld.operator, 0, \modifier4SourceA),
			BlofeldParam.new(\modifier4Constant, 260, nil, Blofeld.m64p63, 64, \modifier4SourceA),
			// modulations
			BlofeldParam.new(\modulation1Src, 261, nil, Blofeld.modSource, 0, \modulation1Amount),
			BlofeldParam.new(\modulation1Dst, 262, nil, Blofeld.modDest, 0, \modulation1Amount),
			BlofeldParam.new(\modulation1Amount, 263, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation2Src, 264, nil, Blofeld.modSource, 0, \modulation2Amount),
			BlofeldParam.new(\modulation2Dst, 265, nil, Blofeld.modDest, 0, \modulation2Amount),
			BlofeldParam.new(\modulation2Amount, 266, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation3Src, 267, nil, Blofeld.modSource, 0, \modulation3Amount),
			BlofeldParam.new(\modulation3Dst, 268, nil, Blofeld.modDest, 0, \modulation3Amount),
			BlofeldParam.new(\modulation3Amount, 269, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation4Src, 270, nil, Blofeld.modSource, 0, \modulation4Amount),
			BlofeldParam.new(\modulation4Dst, 271, nil, Blofeld.modDest, 0, \modulation4Amount),
			BlofeldParam.new(\modulation4Amount, 272, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation5Src, 273, nil, Blofeld.modSource, 0, \modulation5Amount),
			BlofeldParam.new(\modulation5Dst, 274, nil, Blofeld.modDest, 0, \modulation5Amount),
			BlofeldParam.new(\modulation5Amount, 275, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation6Src, 276, nil, Blofeld.modSource, 0, \modulation6Amount),
			BlofeldParam.new(\modulation6Dst, 277, nil, Blofeld.modDest, 0, \modulation6Amount),
			BlofeldParam.new(\modulation6Amount, 278, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation7Src, 279, nil, Blofeld.modSource, 0, \modulation7Amount),
			BlofeldParam.new(\modulation7Dst, 280, nil, Blofeld.modDest, 0, \modulation7Amount),
			BlofeldParam.new(\modulation7Amount, 281, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation8Src, 282, nil, Blofeld.modSource, 0, \modulation8Amount),
			BlofeldParam.new(\modulation8Dst, 283, nil, Blofeld.modDest, 0, \modulation8Amount),
			BlofeldParam.new(\modulation8Amount, 284, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation9Src, 285, nil, Blofeld.modSource, 0, \modulation9Amount),
			BlofeldParam.new(\modulation9Dst, 286, nil, Blofeld.modDest, 0, \modulation9Amount),
			BlofeldParam.new(\modulation9Amount, 287, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation10Src, 288, nil, Blofeld.modSource, 0, \modulation10Amount),
			BlofeldParam.new(\modulation10Dst, 289, nil, Blofeld.modDest, 0, \modulation10Amount),
			BlofeldParam.new(\modulation10Amount, 290, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation11Src, 291, nil, Blofeld.modSource, 0, \modulation11Amount),
			BlofeldParam.new(\modulation11Dst, 292, nil, Blofeld.modDest, 0, \modulation11Amount),
			BlofeldParam.new(\modulation11Amount, 293, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation12Src, 294, nil, Blofeld.modSource, 0, \modulation12Amount),
			BlofeldParam.new(\modulation12Dst, 295, nil, Blofeld.modDest, 0, \modulation12Amount),
			BlofeldParam.new(\modulation12Amount, 296, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation13Src, 297, nil, Blofeld.modSource, 0, \modulation13Amount),
			BlofeldParam.new(\modulation13Dst, 298, nil, Blofeld.modDest, 0, \modulation13Amount),
			BlofeldParam.new(\modulation13Amount, 299, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation14Src, 300, nil, Blofeld.modSource, 0, \modulation14Amount),
			BlofeldParam.new(\modulation14Dst, 301, nil, Blofeld.modDest, 0, \modulation14Amount),
			BlofeldParam.new(\modulation14Amount, 302, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation15Src, 303, nil, Blofeld.modSource, 0, \modulation15Amount),
			BlofeldParam.new(\modulation15Dst, 304, nil, Blofeld.modDest, 0, \modulation15Amount),
			BlofeldParam.new(\modulation15Amount, 305, nil, Blofeld.m64p63, 64),
			BlofeldParam.new(\modulation16Src, 306, nil, Blofeld.modSource, 0, \modulation16Amount),
			BlofeldParam.new(\modulation16Dst, 307, nil, Blofeld.modDest, 0, \modulation16Amount),
			BlofeldParam.new(\modulation16Amount, 308, nil, Blofeld.m64p63, 64),
			// arp
			BlofeldParam.new(\arpMode, 311, 14, Blofeld.arpMode),
			BlofeldParam.new(\arpPattern, 312, nil, (0..16), 0, \arpMode),
			BlofeldParam.new(\arpClock, 314, nil, (0..42), 0, \arpMode),
			BlofeldParam.new(\arpLength, 315, 13, (0..43), 0, \arpMode),
			BlofeldParam.new(\arpOctave, 316, 12, (0..9), 0, \arpMode),
			BlofeldParam.new(\arpDirection, 317, nil, Blofeld.arpDirection, 0, \arpMode),
			BlofeldParam.new(\arpSortOrder, 318, nil, (0..5), 0, \arpMode),
			BlofeldParam.new(\arpVelocity, 319, nil, (0..6), 0, \arpMode),
			BlofeldParam.new(\arpTimingFactor, 320, nil, (0..6), 0, \arpMode),
			BlofeldParam.new(\arpPtnReset, 322, nil, Blofeld.onOff, 0, \arpMode),
			BlofeldParam.new(\arpPtnLength, 323, nil, (0..15), 0, \arpMode),
			BlofeldParam.new(\arpTempo, 326, nil, (0..9), 0, \arpMode),
			//name
			BlofeldParam.new(\nameChar1, 363, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar2, 364, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar3, 365, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar4, 366, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar5, 367, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar6, 368, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar7, 369, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar8, 370, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar9, 371, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar10, 372, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar11, 373, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar12, 374, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar13, 375, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar14, 376, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar15, 377, nil, Blofeld.ascii),
			BlofeldParam.new(\nameChar16, 378, nil, Blofeld.ascii),
			// category
			BlofeldParam.new(\category, 379, nil, Blofeld.category, -1),

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

		multiParams = [
			// multi data
			BlofeldMultiParam.new(\multiName1, 0, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName2, 1, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName3, 2, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName4, 3, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName5, 4, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName6, 5, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName7, 6, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName8, 7, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName9, 8, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName10, 9, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName11, 10, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName12, 11, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName13, 12, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName14, 13, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName15, 14, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiName16, 15, nil, Blofeld.ascii),
			BlofeldMultiParam.new(\multiVolume, 17, nil, (0..127)),
			BlofeldMultiParam.new(\multiTempo, 18, nil, Blofeld.tempo, 54),
		];
		// 16 parts
		16.do { |i|
			var j = i+1;
			multiParams = multiParams.addAll([
				BlofeldMultiParam.new(("bank"++j).asSymbol, 32+(24*i), nil, Blofeld.bank),
				BlofeldMultiParam.new(("sound"++j).asSymbol, 33+(24*i), nil, (0..127)),
				BlofeldMultiParam.new(("vol"++j).asSymbol, 34+(24*i), nil, (0..127)),
				BlofeldMultiParam.new(("pan"++j).asSymbol, 35+(24*i), nil, Blofeld.m64p63, 64),
				BlofeldMultiParam.new(("transpose"++j).asSymbol, 37+(24*i), nil, Blofeld.m64p63, 64),
				BlofeldMultiParam.new(("detune"++j).asSymbol, 38+(24*i), nil, Blofeld.m64p63, 64),
				BlofeldMultiParam.new(("chan"++j).asSymbol, 39+(24*i), nil, Blofeld.channel),
				BlofeldMultiParam.new(("lowVel"++j).asSymbol, 42+(24*i), nil, (1..127)),
				BlofeldMultiParam.new(("highVel"++j).asSymbol, 43+(24*i), nil, (1..127)),
			]);
		};

		globalParams = [
			// global data
			BlofeldGlobalParam.new(\multiMode, 1, nil, (0..1)),
			BlofeldGlobalParam.new(\multiBank1, 2, nil, (0..7)), // these seem to be read-only
			BlofeldGlobalParam.new(\multiSound1, 3, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank2, 4, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound2, 5, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank3, 6, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound3, 7, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank4, 8, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound4, 9, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank5, 10, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound5, 11, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank6, 12, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound6, 13, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank7, 14, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound7, 15, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank8, 16, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound8, 17, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank9, 18, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound9, 19, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank10, 20, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound10, 21, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank11, 22, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound11, 23, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank12, 24, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound12, 25, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank13, 26, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound13, 27, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank14, 28, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound14, 29, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank15, 30, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound15, 31, nil, (0..127)),
			BlofeldGlobalParam.new(\multiBank16, 32, nil, (0..7)),
			BlofeldGlobalParam.new(\multiSound16, 33, nil, (0..127)),
			BlofeldGlobalParam.new(\autoEdit, 35, nil, (0..1)),
			BlofeldGlobalParam.new(\midiChannel, 36, nil, (0..16)), // omni..16
			BlofeldGlobalParam.new(\deviceID, 37, nil, (0..126)),
			BlofeldGlobalParam.new(\popupTime, 38, nil, (1..127)), // 0.1s..15.5s
			BlofeldGlobalParam.new(\contrast, 39, nil, (0..127)),
			BlofeldGlobalParam.new(\masterTune, 40, nil, (54..74)), // 430..450
			BlofeldGlobalParam.new(\transpose, 41, nil, (52..76)), // -12..+12
			BlofeldGlobalParam.new(\ctrlSend, 44, nil, (0..3)),
			BlofeldGlobalParam.new(\ctrlReceive, 45, nil, (0..1)),
			BlofeldGlobalParam.new(\clock, 48, nil, (0..1)), // auto, internal
			BlofeldGlobalParam.new(\velCurve, 50, nil, (0..8)), // linear..fix
			BlofeldGlobalParam.new(\controlW, 51, nil, (0..120)),
			BlofeldGlobalParam.new(\controlX, 52, nil, (0..120)),
			BlofeldGlobalParam.new(\controlY, 53, nil, (0..120)),
			BlofeldGlobalParam.new(\controlZ, 54, nil, (0..120)),
			BlofeldGlobalParam.new(\volume, 55, 7, (0..127)),
			BlofeldGlobalParam.new(\catFilter, 56, nil, Blofeld.catFilter),
		];
		this.initGroups();
	}

	*initGroups {
		byName = ();
		multiByName = ();
		globalByName = ();
		paramSysex = ();
		multiSysex = ();
		globalSysex = ();
		byControl = ();
		params.do { |value|
			byName.put(value.name, value);
			paramSysex.put(value.sysex ? -1, value);
			if (value.control != nil, {
				byControl.put(value.control, value);
			});
		};
		multiParams.do { |value|
			multiByName.put(value.name, value);
			multiSysex.put(value.sysex ? -1, value);
		};
		globalParams.do { |value|
			globalByName.put(value.name, value);
			globalSysex.put(value.sysex ? -1, value);
		};
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

	*new { |name, sysex, control, values, defaultValue = 0, relParam|
		^super.newCopyArgs(name, sysex, control, values, defaultValue, relParam);
	}

	rand {
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
				BlofeldParam.byName[relParam].isSet(data);
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

BlofeldMultiParam : BlofeldParam {
	*new { |name, sysex, control, values, defaultValue = 0|
		^super.newCopyArgs(name, sysex, control, values, defaultValue);
	}
}

BlofeldGlobalParam : BlofeldParam {
	*new { |name, sysex, control, values, defaultValue = 0|
		^super.newCopyArgs(name, sysex, control, values, defaultValue);
	}
}
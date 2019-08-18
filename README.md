# Blofeld quark
SuperCollider quark for working with Waldorf Blofeld

### connect
```supercollider
// it's much better to use Blofeld's MIDIIn port for SuperColler's MIDIOut, instead of USB
// also connect the USB cable to receive sysex messages coming from Blofeld
~blofeld = Blofeld.new.connect("Blofeld", ""); // deviceName, portName
```
### select a random sound
```supercollider
var bank = rrand(0, 7);
var program = rrand(0, 127);
~blofeld.selectSound(bank, program);
```
### request sound
```supercollider
// requires connected USB or MIDIOut from Blofeld
~blofeld.requestSound({|sound|
	sound.printInfo;
	//sound.printInfo(true); // print full info
	sound.getParam(\filter1Type).postln;
});
// print implemented param names
BlofeldParam.byName.keys.postln;
```
### init sound
```supercollider
~blofeld.initSound();
```
### randomize sound
```supercollider
~blofeld.randomizeSound(); // randomize everything
~blofeld.randomizeSound(\filter1); // randomize filter1 only
```
### change single param
```supercollider
~blofeld.setParam(\filter1Type, rrand(0, 10));
~blofeld.setParam(\filter1Cutoff, rrand(0, 127));
```
### filter example
```supercollider
(
~blofeld = Blofeld.new.connect("Blofeld", "");
~blofeld.selectSound(1, 90); // select B091 (Clavinetro)
~blofeld.requestSound({ // request selected sound parameter values
	~cutoff = ~blofeld.getParam(\filter1Cutoff);
	Pdef(\blofeldFilterExample, Ppar([
		Pspawn(Pbind(
			\method, \par,
			\pattern, Pfunc {
				// play random notes from Minor scale
				// change \filter1Resonance and ~cutoff target on every new note
				Pbind(
					\type, \blofeld,
					\midicmd, \noteOn,
					\scale, Scale.minor,
					\degree, Prand(Scale.minor.degrees, inf),
					\octave, Pwhite(2, 5, inf),
					\dur, 10,
					\filter1Resonance, Pwhite(0, 96, inf),
					\setCutoff, Pfunc {
						~cutoff = rrand(0, 127);
					},
				);
			},
			\delta, Pwhite(32, 128, inf)
		)),
		Pbind(
			\type, \blofeld,
			\dur, 0.25,
			\filter1Cutoff, Pfunc { |event|
				var speed = 1/3;
				var current = ~blofeld.getParam(\filter1Cutoff);
				current + ((~cutoff - current) * speed); // animate ~cutoff to target value
			},
		),
		Pbind(
			\type, \blofeld,
			\dur, 1,
			\effect2Mix, Pfunc {
				var value = ~blofeld.getParam(\effect2Mix) + 1;
				if (value > 127, { 0 }, { value }); // increase click-delay mix every second
			},
		),
	])).play;
});
)
Pdef(\blofeldFilterExample).stop;
```
### wavetable example
```supercollider
(
// connect
~blofeld = Blofeld.new.connect("Blofeld", "");
)

(
// create 10 wavetables with increasing complexity in first 10 user slots
~createWavetable = { |i|
	// from: http://sccode.org/1-5bF#c876
	//random number of envelope segments
	var numSegs = i.linexp(0,9,4,40).round;
	// make every wave slightly different
	var wavetable = ~blofeld.createWavetable({
		//env always begins and ends with zero
		//inner points are random from -1.0 to 1.0
		[0]++({1.0.rand}.dup(numSegs-1) * [1,-1]).scramble++[0]
	}, {
		//greater segment duration variety in higher-index wavetables
		{exprand(1,i.linexp(0,9,1,50))}.dup(numSegs)
	}, {
		//low-index wavetables tend to be sinusoidal
		//high index wavetables tend to have sharp angles and corners
		{[\sine,0,exprand(1,20) * [1,-1].choose].wchoose([9-i,3,i].normalizeSum)}.dup(numSegs)
	});
	~blofeld.sendWavetable(80+i, wavetable, "new sine wt"++(i+1));
};
r = Routine({
	10.do { |i|
		("creating wt"+i).postln;
		~createWavetable.value(i);
		12.wait;
	}
});
r.play;
)

(
// multimode needs to be switched on manually
//~blofeld.multiMode(true); // <- will give you an error
// initialize first 10 channels
r = Routine({
	10.do({ |i|
		("init sound"+i).postln;
		~blofeld.initSound(Blofeld.editBuffer, i);
		1.wait;
	});
});
r.play;
)

(
// patterns from: http://sccode.org/1-5bF#c876
t = TempoClock.new(90/60).permanent_(true);
~shapes = [Blofeld.shape[\sine]]++(Blofeld.shape[\user1]..Blofeld.shape[\user9]);
~blofeld.clearEditBuffer((0..9));
Pdef(\pad, Pbind(
	\type, \blofeld,
	\osc1Shape, Prand(~shapes[0..3], inf),
	\osc1FMSource, Blofeld.fmSource[\noise],
	\osc1FM, Pwhite(4, 12),
	\osc2Shape, Prand(~shapes[0..3], inf),
	\osc2FMSource, Blofeld.fmSource[\noise],
	\osc2FM, Pwhite(4, 12),
	\ampEnvAttack, Pexprand(36, 72),
	\ampEnvAttackLevel, 127,
	\ampEnvDecay, 127,
	\ampEnvSustain, 0,
	\ampEnvRelease, Pexprand(60, 120),
	\effect2Type, Blofeld.effect[\reverb],
	\effect2Mix, 63,
	\lfo1Shape, Blofeld.lfoShape[\random],
	\lfo1Speed, Pwhite(1, 8),
	\lfo1StartPhase, rrand(0, 127),
	\lfo2Shape, Blofeld.lfoShape[\random],
	\lfo2Speed, Pwhite(1, 8),
	\lfo2StartPhase, rrand(0, 127),
	\lfo3Shape, Blofeld.lfoShape[\random],
	\lfo3Speed, 20,
	\lfo3StartPhase, rrand(0, 127),
	\modulation1Src, Blofeld.modSource[\lfo1],
	\modulation1Dst, Blofeld.modDest[\osc1Pitch],
	\modulation1Amount, Pwhite(55, 71), // -8..+8
	\modulation2Src, Blofeld.modSource[\lfo2],
	\modulation2Dst, Blofeld.modDest[\osc2Pitch],
	\modulation2Amount, Pwhite(55, 71),
	\modulation3Src, Blofeld.modSource[\lfo3],
	\modulation3Dst, Blofeld.modDest[\f1Pan],
	\modulation3Amount, 127,
	// midi
	\midicmd, \noteOn,
	\scale, Scale.minorPentatonic,
	\degree, Pfunc({
		(-12,-10..12).scramble[0..rrand(1,3)]
	}),
	\amp, Pexprand(0.05,0.07)*4,
	\dur, Pwrand([1,4,6,9,12],[0.35,0.25,0.2,0.15,0.05],inf),
	\chan, 0,
)).play;

Pdef(\pulse, Pbind(
	\type, \blofeld,
	\osc1Shape, Prand(~shapes[4..9], inf),
	\osc1FMSource, Blofeld.fmSource[\noise],
	\osc1FM, 3,
	\ampEnvAttack, 1,
	\ampEnvDecay, 127,
	\ampEnvSustain, 0,
	\ampEnvRelease, Pexprand(12,36),
	\effect2Mix, 48,
	\lfo1Shape, Blofeld.lfoShape[\random],
	\lfo1Speed, Pwhite(1, 63),
	\lfo3Shape, Blofeld.lfoShape[\random],
	\lfo3Speed, 10,
	\lfo3StartPhase, rrand(0, 127),
	\modulation1Src, Blofeld.modSource[\lfo1],
	\modulation1Dst, Blofeld.modDest[\osc1Pitch],
	\modulation1Amount, Pwhite(50, 76), // -8..+8
	\modulation3Src, Blofeld.modSource[\lfo3],
	\modulation3Dst, Blofeld.modDest[\f1Pan],
	\modulation3Amount, 127,
	// midi
	\midicmd, \noteOn,
	\scale, Scale.minorPentatonic,
	\degree, Pseq([Prand([-15,-10,-5],24), Pseq([\],1)],inf) + Pstutter(25,Pwrand([0,2,-1],[0.78,0.1,0.12],inf)),
	\amp, Pseq([Pgeom(0.45,-1.dbamp,25)],inf)*2,
	\dur, Pseq([
		Pstutter(24,Pseq([1/4],1)),
		Prand([1,2,4,6,12],1)
	],inf),
	\chan, 1,
)).play(t, quant: 1);

Pdef(\melody, Pbind(
	\type, \blofeld,
	\osc1Shape, Pwrand([
		Pseq([~shapes[0]],4),
		Pseq([~shapes[1]],4),
		Pseq([~shapes[2]],4),
	],[9,3,1].normalizeSum,inf),
	\osc1FMSource, Blofeld.fmSource[\noise],
	\osc1FM, 3,
	\ampEnvAttack, 0,
	\ampEnvSustain, 7,
	\ampEnvDecay, 127,
	\ampEnvRelease, 36,
	\effect2Mix, 63,
	\lfo1Shape, Blofeld.lfoShape[\random],
	\lfo1Speed, 10,
	\modulation1Src, Blofeld.modSource[\lfo1],
	\modulation1Dst, Blofeld.modDest[\osc1Pitch],
	\modulation1Amount, Pwhite(45, 81), // -8..+8
	// midi
	\midicmd, \noteOn,
	\midinote, Pxrand([
		Pseq([\,67,60,Prand([58,70,\])],1),
		Pseq([\,67,58,Prand([57,63,\])],1),
		Pseq([\,70,72,Prand([65,79,\])],1)
	],inf),
	\amp, Pseq([0,0.18,0.24,0.28],inf)*2,
	\dur, Prand([
		Pseq([Prand([12,16,20]),2,1.5,0.5],1),
		Pseq([Prand([12,16,20]),1.5,1,1.5],1),
	],inf),
	\chan, 2,
)).play(t, quant:1);

//infinite sequence of various finite rhythmic patterns
//all very short envelopes
Pdef(\rhythms,
	Pwrand([
		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(4, Prand(~shapes[5..9],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, 0,
			\ampEnvDecay, 127,
			\ampEnvRelease, Pstutter(2, Pexprand(0,1)),
			\effect2Mix, Pwhite(0, 32),
			\lfo1Shape, Blofeld.lfoShape[\random],
			\lfo1Speed, 1,
			\osc1FMSource, Blofeld.fmSource[\lfo1],
			\osc1FM, 100,
			// midi
			\midicmd, \noteOn,
			\dur, Pseq([1/8],4),
			\freq, Pstutter(4, Prand([
				Pexprand(10000,20000,1),
				Pexprand(100,200,1),
				Pexprand(1,2,1)
			],inf)),
			\amp, Pgeom(0.9, -6.dbamp, 4) * Pstutter(4,Pexprand(0.3,1)),
			\chan, 3,
		),

		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(2, Prand(~shapes[8..9],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, 0,
			\ampEnvDecay, 127,
			\ampEnvRelease, Pstutter(2, Pexprand(1,3)),
			\effect2Mix, Pwhite(0,32),
			\lfo1Shape, Blofeld.lfoShape[\random],
			\lfo1Speed, 1,
			\osc1FMSource, Blofeld.fmSource[\lfo1],
			\osc1FM, Pstutter(2, Pexprand(10,200)),
			// midi
			\midicmd, \noteOn,
			\dur, Pseq([1/4],2),
			\freq, Pstutter(2, Pexprand(1,200)),
			\amp, Pgeom(0.4, -3.dbamp, 2)  * Pexprand(0.4,1),
			\chan, 4,
		),

		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(6, Prand(~shapes[2..5],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, Pseq([4,0,0],1),
			\ampEnvDecay, 127,
			\ampEnvRelease, Pseq([0,Pexprand(1,7,48)],1),
			\effect2Mix, Pwhite(0,20),
			\lfo1Shape, Blofeld.lfoShape[\random],
			\lfo1Speed, 1,
			\osc1FMSource, Blofeld.fmSource[\lfo1],
			\osc1FM, 100,
			// midi
			\midicmd, \noteOn,
			\dur, Pseq([1/2,1/4,1/4],1),
			\freq, Pstutter(6, Pexprand(1000,2000)),
			\amp, Pseq([0.1,0.5,0.3],1),
			\chan, 5,
		),

		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(6, Prand(~shapes[2..5],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, Pseq([0,4,0],1),
			\ampEnvDecay, 127,
			\ampEnvRelease, Pseq([Pexprand(0,7,24),0,Pexprand(0,7,24)],1),
			\effect2Mix, Pwhite(0,20),
			\lfo1Shape, Blofeld.lfoShape[\random],
			\lfo1Speed, 1,
			\osc1FMSource, Blofeld.fmSource[\lfo1],
			\osc1FM, 100,
			// midi
			\midicmd, \noteOn,
			\dur, Pseq([1/4,1/2,1/4],1),
			\freq, Pstutter(6, Pexprand(1000,2000)),
			\amp, Pseq([0.5,0.1,0.4],1),
			\chan, 6,
		),

		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(6, Prand(~shapes[8..9],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, 0,
			\ampEnvDecay, 127,
			\ampEnvRelease, Pstutter(6, Pexprand(0,1)),
			\effect2Mix, Pwhite(0,20),
			\lfo1Shape, Blofeld.lfoShape[\random],
			\lfo1Speed, 1,
			\osc1FMSource, Blofeld.fmSource[\lfo1],
			\osc1FM, Pstutter(6, Pexprand(10,100)),
			// midi
			\midicmd, \noteOn,
			\dur, Pseq([1/6],6),
			\freq, Pstutter(6, Pexprand(10,200)),
			\amp, Pgeom(0.7, -4.dbamp, 6)  * Pexprand(0.4,1),
			\chan, 7,
		),

		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(2, Prand(~shapes[8..9],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, 0,
			\ampEnvDecay, 127,
			\ampEnvRelease, Pstutter(2, Pexprand(0,5)),
			\effect2Mix, Pwhite(7,63),
			\lfo1Shape, Blofeld.lfoShape[\random],
			\lfo1Speed, 1,
			\osc1FMSource, Blofeld.fmSource[\lfo1],
			\osc1FM, Pstutter(2, Pexprand(10,100)),
			// midi
			\midicmd, \noteOn,
			\dur, Prand([
				Pseq([1/2],2),
				Pseq([1],2),
				Pseq([1,1/2,1/2],1),
				Pseq([2],1),
			],1),
			\freq, Pstutter(2, Pexprand(10,200)),
			\amp, 0.5,
			\chan, 8,
		),

		Pbind(
			\type, \blofeld,
			\osc1Shape, Pstutter(16, Prand(~shapes[0..9],inf)),
			\ampEnvAttack, 0,
			\ampEnvSustain, 0,
			\ampEnvDecay, 127,
			\ampEnvRelease, Pexprand(0,1),
			\effect2Mix, 0,
			// midi
			\midicmd, \noteOn,
			\dur, Prand([
				Pseq([1/16],16),
				Pseq([1/16],8)
			],1),
			\freq, Pstutter(16,Pexprand(1000,20000,inf)),
			\amp, 0.13,
			\chan, 9,
		),
	],
	[40,18,3,3,15,25,5].normalizeSum, inf)
).play(t,quant:1);
)

(
// stop everything
Pdef(\pad).stop;
Pdef(\pulse).stop;
Pdef(\melody).stop;
Pdef(\rhythms).stop;
)
```

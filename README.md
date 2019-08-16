# Blofeld
[WIP] SuperCollider quark for working with Blofeld

### connect
```supercollider
~blofeld = Blofeld.new("Blofeld", ""); // deviceName, portName
```
### select a random sound
```supercollider
var bank = rrand(0, 7);
var program = rrand(0, 127);
~blofeld.selectSound(bank, program);
```
### request sound
```supercollider
~blofeld.requestSound();
~blofeld.getParam(\filter1Type).postln;
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
### full example
```supercollider
(
~blofeld = Blofeld.new("Blofeld", "");
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

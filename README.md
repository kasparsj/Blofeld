# Blofeld
[WIP] SuperCollider quark for working with Blofeld

### connect
```supercollider
~blofeld = Blofeld.new();
~blofeld.connect("Blofeld", "");
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
// 1. selects a random sound sound
// 2. using Pspawn in parallel:
// - play a new note from minor scale
// - change filter 1 resonance to a random value
// - sets a new target cutoff frequency for filter 1 to animate to
// 3. animate effect2Mix every second
(
~blofeld = Blofeld.new();
~blofeld.connect("Blofeld", "");
~blofeld.selectSound(1, 90); // B091
~blofeld.requestSound();
~cutoff = ~blofeld.getParam(\filter1Cutoff);
p = Pspawn(Pbind(
	\method, \par,
	\pattern, Pfunc {
		Pbind(
			\type, \midi,
			\midicmd, \noteOn,
			\midiout, ~blofeld.midiOut,
			\scale, Scale.minor,
			\degree, Prand(Scale.minor.degrees, inf),
			\octave, Pwhite(2, 5, inf),
			\dur, 10,
			\setCutoff, Pfunc{
				~cutoff = rrand(0, 127);
				~resonance = rrand(0, 127);
			}
		);
	},
	\delta, Pwhite(32, 128, inf)
)).play;
)
p.stop;

(
r = Routine({
	loop {
		var speed = 1/3;
		var current = ~blofeld.getParam(\filter1Cutoff);
		var value = current + ((~cutoff - current) * speed);
		~blofeld.setParam(\filter1Cutoff, value);
		~blofeld.setParam(\filter1Resonance, ~resonance);
		0.25.wait;
	}
}).play;
)
r.stop;

(
d = Routine({
	loop {
		var value = ~blofeld.getParam(\effect2Mix) + 1;
		~blofeld.setParam(\effect2Mix, if (value > 127, { 0 }, { value }));
		1.wait;
	}
}).play;
)
d.stop;
```

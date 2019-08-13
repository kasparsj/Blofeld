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
### full example filter1
```supercollider
// 1. selects a random sound sound
// 2. every 10 seconds:
// - plays a new note from minor scale
// - changes filter 1 resonance
// - sets a new target cutoff frequence for filter 1
(
~blofeld = Blofeld.new();
~blofeld.connect("Blofeld", "");
~blofeld.selectSound(1, 90); // B091 Clavinetro
~blofeld.requestSound({
	~cutoff = ~blofeld.getParam(\filter1Cutoff);
	~blofeld.noteOn(60, 127);
	p = Pspawn(Pbind(
		\method, \par,
		\pattern, Pfunc {
			Pbind(
				\type, \midi,
				\midicmd, \noteOn,
				\midiout, ~blofeld.midiOut,
				\scale, Scale.minor,
				\degree, Prand(Scale.minor.degrees, inf),
				\dur, 10,
				\setCutoff, Pfunc{
					~cutoff = rrand(0, 127);
					~resonance = rrand(0, 127);
				}
			);
		},
		\delta, Pwhite(16, 64, inf)
	)).play;
});
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
~blofeld.noteOff();
```

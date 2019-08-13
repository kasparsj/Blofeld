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
BlofeldParam.byName.do({ |p| p.asString.postln; });
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
### change one param
```supercollider
~blofeld.setParam(\filter1Type, rrand(0, 10));
~blofeld.setParam(\filter1Cutoff, rrand(0, 127));
```

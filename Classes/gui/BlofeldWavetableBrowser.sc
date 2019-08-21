BlofeldWavetableBrowser {
	var <blofeld;
	var window;
	var filesMenu, v, iv, dv;
	var nameField, slotMenu, uploadBtn, osc1Btn;
	var keyboard;
	var names, soundfile, index = 0, currentWave, currentWavetable, buffer, synth;

	*new { |blofeld|
		^super.newCopyArgs(blofeld).init;
	}

	init {
		window = Window("Blofeld wavetable browser");
		window.layout = VLayout();
		window.front;

		window.view.keyDownAction = { |doc, char, mod, unicode, keycode, key|
			var i;
			switch(keycode,
				126, { this.prevFile; },
				125, { this.nextFile; },
				123, { this.prevWave; },
				124, { this.nextWave; },
				49, { this.play; }, // space
				{
					if(char.isAlpha) {
						i = names.detectIndex { |x| x.asString[0] == char };
						i !? { filesMenu.value = i }
					}
				}
			);
			this.update;

		};

		this.createHeader;
		this.createBody;
		this.createFooter;
		this.createKeyboard;

		filesMenu.action.value(filesMenu);
		this.update;
	}

	createHeader {
		filesMenu = PopUpMenu(window);
		filesMenu.items = names = BlofeldWavetable.files.keys.asArray.sort;
		filesMenu.action = { |v|
			if (currentWavetable != nil) {
				if (currentWavetable.name != v.item.asSymbol, { index = 0; });
			};
			currentWavetable = BlofeldWavetable.load(BlofeldWavetable.files[v.item.asSymbol], false, false);
			currentWave = currentWavetable.getWave(index).copy;
			this.load(currentWave);
		};
		filesMenu.value = 0;
		iv = StaticText(window).maxWidth_(80);
		dv = StaticText(window).maxWidth_(100);
		window.layout.add(HLayout(filesMenu, iv, dv));
	}

	createBody {
		v = SoundFileView(window);
		window.layout.add(v);
	}

	createFooter {
		nameField = TextField(window);
		nameField.action = { |v|
			currentWavetable.displayName = v.item.asString;
		};
		slotMenu = PopUpMenu(window);
		slotMenu.items = (80..118);
		slotMenu.value = 0;
		slotMenu.action = { |v|
			currentWavetable.slot = v.item.asInteger;
		};
		uploadBtn = Button(window).string_("Upload");
		uploadBtn.action = { |v|
			blofeld.upload(currentWavetable);
		};
		osc1Btn = Button(window).string_("Osc1");
		osc1Btn.action = { |v|
			blofeld.editBuffer.set(\osc1Shape, slotMenu.value + 86);
		};
		window.layout.add(HLayout(nameField, slotMenu, uploadBtn, osc1Btn));
	}

	createKeyboard {
		keyboard = MIDIKeyboardView(window);
		keyboard.keyDownAction_({ |note|
			blofeld.midiOut.noteOn(0, note, 60);
		});
		keyboard.keyUpAction_({ |note|
			blofeld.midiOut.noteOff(0, note, 60);
		});
		keyboard.setColor(60, Color.red);
		window.layout.add(HLayout(keyboard));
	}

	load { |wave|
		v.setData(wave.normalize.scale(2).offset(-1));
		nameField.string = currentWavetable.displayName;
		slotMenu.valueAction_(currentWavetable.slot - 80);
		uploadBtn.enabled_(currentWavetable.validate);
	}

	update {
		iv.string = "wave: % / %".format((index+1).asString, currentWavetable.numWaves);
		dv.string = "% frames".format(currentWave.size);
	}

	prevFile {
		filesMenu.value = filesMenu.value - 1 % names.size;
		filesMenu.action.value(filesMenu);
	}

	nextFile {
		filesMenu.value = filesMenu.value + 1 % names.size;
		filesMenu.action.value(filesMenu);
	}

	prevWave {
		index = index - 1 % currentWavetable.numWaves;
		filesMenu.action.value(filesMenu);
	}

	nextWave {
		index = index + 1 % currentWavetable.numWaves;
		filesMenu.action.value(filesMenu);
	}

	play {
		buffer = Buffer.alloc(Server.default, currentWave.size*2, 1, {
			arg buf, index;
			buf.setnMsg(0, currentWave.asWavetable);
		});
		synth = SynthDef(\playwavetable, {
			arg buf=0, freq=440, detune=0.2,
			amp=0.2, pan=0, out=0,
			atk=0.01, sus=1, rel=0.01, c0=1, c1=(-1);

			var sig, env, detuneCtrl;
			env = EnvGen.ar(
				Env([0,1,1,0],[atk,sus,rel],[c0,0,c1]),
				doneAction:2
			);
			detuneCtrl = LFNoise1.kr(0.1).bipolar(detune).midiratio;
			sig = Osc.ar(buf, freq * detuneCtrl);
			sig = LeakDC.ar(sig); //remove DC bias
			sig = Pan2.ar(sig, pan, amp); //L/R balance (pan)
			sig = sig * env;
			Out.ar(out, sig);
		}).play(Server.default, [
			buf: buffer.bufnum,
			atk: 0.1,
			sus: 0.3,
			rel: 1,
			c0: -2,
			c1: -2,
			detune: rrand(0.18,0.25),
		]);
	}
}
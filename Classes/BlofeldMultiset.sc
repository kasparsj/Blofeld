BlofeldMultiset {
	var <blofeld;
	var <multis;

	*new { |blofeld = nil, multis = nil|
		if (multis.isCollection.not, {
			multis = ();
		});
		^super.newCopyArgs(blofeld);
	}

	getOrCreate { |slot, bank = 0x00|
		var key = Blofeld.key(bank, slot);
		var multi = multis[key];
		if (multi == nil, {
			multi = BlofeldMulti.new(slot, bank);
			this.add(multi);
		});
		^multi;
	}

	get { |slot, bank = 0x00|
		^multis[Blofeld.key(bank, slot)];
	}

	getByName { |name|
		^multis.detect({ |multi|
			multi.getName() == name;
		});
	}

	choose {
		^multis.choose;
	}

	add { |multi|
		multi.put(Blofeld.key(multi.bank, multi.slot), multi);
		multi.multiset = this;
	}

	remove { |slot, bank = 0x00|
		var key = Blofeld.key(bank, slot);
		multis[key].multiset = nil;
		multis.removeAt(key);
	}

	download { |multi, callback = nil|
		blofeld.multiRequest(multi.slot, multi.bank, this.onMultiDump(callback));
	}

	downloadAll { |callback|
		var r = Routine({
			blofeld.multiRequest(0x00, Blofeld.allMultisBank, this.onMultiDump);
			(Blofeld.bank.size*16).wait;
			if (callback != nil, { callback.value(this) });
		});
		r.play;
		^r;
	}

	upload { |multi, callback = nil|
		var r = Routine({
			blofeld.multiDump(multi);
			this.add(multi);
			if (callback != nil, {
				1.wait;
				callback.value;
			});
		});
		r.play;
		^r;
	}

	uploadAll { |callback = nil|
		var r = Routine({
			16.do { |i|
				var multi = this.get(i);
				if (multi != nil, {
					this.upload(multi);
					1.wait;
				});
			};
			if (callback != nil, { callback.value(this) });
		});
		r.play;
		^r;
	}

	onMultiDump { |callback = nil|
		^{|bank, slot, data|
			var multi = this.getOrCreate(slot, bank);
			multi.data = data;
			if (callback != nil, { callback.value(multi); });
		}
	}
}
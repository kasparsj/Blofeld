BlofeldSysex {
	const <sysexBegin = 0xF0;
	const <sysexEnd = 0xF7;
	const <waldorfID = 0x3E;
	const <blofeldID = 0x13;
	const <soundRequestIDM = 0x00;
	const <multiRequestIDM = 0x01;
	const <wavetableRequestIDM = 0x02;
	const <globalRequestIDM = 0x04;
	const <fwVersionRequestIDM = 0x0F;
	const <soundDumpIDM = 0x10;
	const <multiDumpIDM = 0x11;
	const <wavetableDumpIDM = 0x12;
	const <globalDumpIDM = 0x14;
	const <paramChangeIDM = 0x20;

	classvar <callbacks;
	classvar <>debug = false;

	*initClass {
		callbacks = ();
		MIDIIn.addFuncTo(\sysex, {|src, sysex|
			this.parseSysex(sysex);
		});
	}

	*parseSysex { |packet|
		if (packet[1] == waldorfID && packet[2] == blofeldID) {
			var deviceID = packet[3];
			switch (packet[4],
				paramChangeIDM, {
					if (debug, {
						var bParam = BlofeldSound.bySysex[(packet[6] * 128 + packet[7])];
						"Param change, loc: %, %: %".format(packet[5], bParam.name, packet[8]).postln;
					});
				},
				soundDumpIDM, {
					var bank = packet[5];
					var program = packet[6];
					var data = packet[7..389];
					var key = Blofeld.soundKey(bank, program, deviceID);
					callbacks[key].value(bank, program, data);
				},
				multiDumpIDM, {
					var bank = packet[5];
					var slot = packet[6];
					var data = packet[7..424];
					var key = Blofeld.multiKey(bank, slot, deviceID);
					callbacks[key].value(slot, bank, data);
				},
				wavetableDumpIDM, {
					Error("Wavetable dump packet received!").throw;
				},
				globalDumpIDM, {
					var data = packet[5..76];
					var key = Blofeld.globalKey(deviceID);
					callbacks[key].value(data);
				}, {
					if (debug, { packet.postln; });
				}
			);
		};
	}

	*soundRequestPacket { |bank, program, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(soundRequestIDM);
		packet = packet.add(bank);
		packet = packet.add(program);
		^packet;
	}

	*paramChangePacket { |param, value, location = 0, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(paramChangeIDM);
		packet = packet.add(location);
		packet = packet.add(param.sysex / 128);
		packet = packet.add(param.sysex % 128);
		packet = packet.add(value);
		^packet;
	}

	*globalRequestPacket { |deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(globalRequestIDM);
		^packet;
	}

	*globalDumpPacket { |global, deviceID = 0x00|
		var data = global.asInt8Array;
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(globalDumpIDM);
		packet = packet.addAll(data);
		packet = packet.add(this.checksum(data));
		^packet;
	}

	*multiRequestPacket { |slot, bank = 0x00, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(multiRequestIDM);
		packet = packet.add(bank);
		packet = packet.add(slot);
		^packet;
	}

	*multiDumpPacket { |multi, deviceID = 0x00|
		var data = multi.asInt8Array;
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(multiDumpIDM);
		packet = packet.addAll(data);
		packet = packet.add(this.checksum(data));
		^packet;
	}

	*wavetableDumpPacket { |slot, samples, ascii, wave, mult = 1, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(wavetableDumpIDM);
		packet = packet.add(0x50 + (slot - 80));
		packet = packet.add(wave & 0x7F);
		packet = packet.add(0x00); // format
		128.do({ |i|
			var sample = (samples[i*mult] * 1048575).asInteger;
			packet = packet.addAll(this.unpack(sample));
		});
		14.do({ |i|
			var char = if(ascii[i] != nil, { ascii[i] & 0x7f }, { 0x00 });
			packet = packet.add(char);
		});
		packet = packet.add(0x00); // reserved
		packet = packet.add(0x00); // reserved
		packet = packet.add(this.checksum(packet[7..407]));
		^packet;
	}

	*soundDumpPacket { |sound, deviceID = 0x00|
		var data = sound.asInt8Array;
		var packet = Int8Array.new;
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(soundDumpIDM);
		packet = packet.addAll(data);
		packet = packet.add(this.checksum(data[2..]));
		^packet;
	}

	*unpack { |value|
		var arr = [];
		// arr = arr.add((value >> 14) & 0x7f);
		// arr = arr.add((value >> 7) & 0x7f);
		// arr = arr.add((value) & 0x7f);
		if (value < 0, {
			arr = arr.add((value & 0x000FC000) >> 14 + 0x40);
			}, {
				arr = arr.add((value & 0x000FC000) >> 14);
		});
		arr = arr.add((value & 0x00003F80) >> 7);
		arr = arr.add((value & 0x0000007F));
		^arr;
	}

	*pack { |byte1, byte2, byte3|
		var value = (byte1 << 14) | (byte2 << 7) | (byte3);
		^value;
	}

	*checksum { |data|
		var csum = data.sum & 0x7F;
		^csum;
	}
}
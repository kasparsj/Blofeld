BlofeldSysex {
	const <sysexBegin = 0xF0;
	const <sysexEnd = 0xF7;
	const <waldorfID = 0x3E;
	const <blofeldID = 0x13;
	const <soundRequest = 0x00;
	const <multiRequest = 0x01;
	const <wavetableRequest = 0x02;
	const <globalRequest = 0x04;
	const <fwVersionRequest = 0x0F;
	const <soundDump = 0x10;
	const <multiDump = 0x11;
	const <wavetableDump = 0x12;
	const <globalDump = 0x14;
	const <paramChange = 0x20;

	classvar <soundDumpCallback;
	classvar <multiDumpCallback;
	classvar <globalDumpCallback;
	classvar <>debug = false;

	*initClass {
		soundDumpCallback = ();
		multiDumpCallback = ();
		globalDumpCallback = ();

		MIDIIn.addFuncTo(\sysex, {|src, sysex|
			this.parseSysex(sysex);
		});
	}

	*parseSysex { |packet|
		if (packet[1] == waldorfID) {
			switch (packet[4],
				paramChange, {
					if (debug, {
						var bParam = BlofeldParam.paramSysex[(packet[6] * 128 + packet[7])];
						"Param change, loc: %, %: %".format(packet[5], bParam.name, packet[8]).postln;
					});
				},
				soundDump, {
					var bank = packet[5];
					var program = packet[6];
					var data = packet[7..389];
					var key;
					if (bank == BlofeldEditBuffer.editBufferBank, {
						key = BlofeldEditBuffer.key(program);
						soundDumpCallback[key].value(program, data);
					}, {
						key = Blofeld.key(bank, program);
						soundDumpCallback[key].value(bank, program, data);
					});
				},
				multiDump, {
					var bank = packet[5];
					var slot = packet[6];
					var data = packet[7..424];
					var key;
					if (bank == BlofeldEditBuffer.editBufferBank, {
						key = BlofeldEditBuffer.key(\multi);
						multiDumpCallback[key].value(data);
					}, {
						key = Blofeld.key(bank, slot);
						multiDumpCallback[key].value(bank, slot, data);
					});
				},
				wavetableDump, {
					Error("Wavetable dump packet received!").throw;
				},
				globalDump, {
					var data = packet[5..76];
					globalDumpCallback[\global].value(data);
				}, {
					if (debug, { packet.postln; });
				}
			);
		};
	}

	*soundRequestPacket { |bank, program, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(soundRequest);
		packet = packet.add(bank);
		packet = packet.add(program);
		packet = packet.add(sysexEnd);
		^packet;
	}

	*paramChangePacket { |param, value, location = 0, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(paramChange);
		packet = packet.add(location);
		packet = packet.add(param.sysex / 128);
		packet = packet.add(param.sysex % 128);
		packet = packet.add(value);
		packet = packet.add(sysexEnd);
		^packet;
	}

	*globalRequestPacket { |deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(globalRequest);
		packet = packet.add(sysexEnd);
		^packet;
	}

	*globalDumpPacket { |data, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(globalDump);
		packet = packet.addAll(data);
		packet = packet.add(this.checksum(data));
		packet = packet.add(sysexEnd);
		^packet;
	}

	*multiRequestPacket { |slot, bank = 0x00, deviceID = 0x00|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(multiRequest);
		packet = packet.add(bank);
		packet = packet.add(slot);
		packet = packet.add(sysexEnd);
		^packet;
	}

	*multiDumpPacket { |multi, deviceID = 0x00, beginAndEnd = true|
		var packet = Int8Array.new();
		if (beginAndEnd, {
			packet = packet.add(sysexBegin);
		});
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(multiDump);
		packet = packet.add(multi.slot);
		packet = packet.add(multi.tempo);
		packet = packet.add(multi.volume);
		multi.sounds.do { |i|
			packet = packet.addAll(multi.sounds[i].data);
		};
		packet = packet.add(this.checksum(packet[5..]));
		if (beginAndEnd, {
			packet = packet.add(sysexEnd);
		});
		^packet;
	}

	*wavetableDumpPacket { |slot, samples, ascii, wave, mult = 1, deviceID = 0x00, beginAndEnd = true|
		var packet = Int8Array.new();
		packet = packet.add(sysexBegin);
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(wavetableDump);
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
		packet = packet.add(sysexEnd);
		^packet;
	}

	*soundDumpPacket { |sound, deviceID = 0x00, beginAndEnd = true|
		var packet = Int8Array.new();
		if (beginAndEnd, {
			packet = packet.add(sysexBegin);
		});
		packet = packet.add(waldorfID);
		packet = packet.add(blofeldID);
		packet = packet.add(deviceID);
		packet = packet.add(soundDump);
		packet = packet.add(sound.bank);
		packet = packet.add(sound.program);
		packet = packet.addAll(sound.data);
		packet = packet.add(this.checksum(sound.data));
		if (beginAndEnd, {
			packet = packet.add(sysexEnd);
		});
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
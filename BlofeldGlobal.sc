BlofeldGlobal {
	var <blofeld;
	var <data;

	*new { |blofeld|
		^super.newCopyArgs(blofeld, Int8Array.new, ());
	}

	get { |param|
		var bParam = BlofeldParam.byName[param];
		var value = if (bParam != nil && bParam.sysex != nil, { data[bParam.sysex] }, { nil });
		^value;
	}

	set { |param, value = 0, upload = false|
		var bParam = BlofeldParam.byName[param];
		value = value.asInteger.min(127).max(0);
		if (bParam != nil && bParam.sysex != nil, {
			data[bParam.sysex] = value;
			if (upload, {
				this.upload();
			});
		});
	}

	download { |callback = nil|
		BlofeldSysex.globalDumpCallback.put(\global, this.expect(callback));
		blofeld.midiOut.sysex(BlofeldSysex.globalRequestPacket(blofeld.deviceID));
	}

	upload { |callback = nil|
		// todo: figure out a way to implement callback
		blofeld.midiOut.sysex(BlofeldSysex.globalDumpPacket(data, blofeld.deviceID));
	}

	multiMode { |value = nil, upload = true|
		var bParam = BlofeldParam.byName[\multiMode];
		value = if (value != nil, {
			//this.setGlobalParam(\multiMode, value, sendGlobal);
			Error("Multimode needs to be switched on manually").throw;
		}, {
			data[bParam.sysex];
		});
		^value;
	}

	catFilter { |value = nil, upload = true|
		var bParam = BlofeldParam.byName[\catFilter];
		value = if (value != nil, {
			this.set(\catFilter, value, upload);
		}, {
			data[bParam.sysex];
		});
		^value;
	}

	expect { |callback = nil|
		^{|packetData|
			data = packetData;
			if (callback != nil, { callback.value(this); });
			BlofeldSysex.globalDumpCallback.removeAt(\global);
		}
	}
}
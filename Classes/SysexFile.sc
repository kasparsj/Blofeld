SysexFile {
	const <byteBegin = -16;
	const <byteEnd = -9;

	var <>pathName;
	var <events;

	*new { |pathName|
		^super.newCopyArgs(pathName, []);
	}

	*read { arg pathName; ^SysexFile( pathName ).read; }

	read { var file, time;
		file = File(pathName,"r");
		this.readFile(file);
	}

	readFile { |theFile|
		var event;
		while (
			{theFile.pos < (theFile.length-1) },
			{
				var val = theFile.getInt8;
				switch (val,
					byteBegin, {
						event = Int8Array.new;
					},
					byteEnd, {
						events = events.add(event);
					}, {
						event = event.add(val);
					}
				);
			}
		);
		theFile.close;
	}

	writeFile { |theFile|
		events.do({ |event|
			theFile.putInt8(byteBegin);
			event.do({ |data| theFile.putInt8(data); });
			theFile.putInt8(byteEnd);
		});

		theFile.close;

	}

	write { |newFileName|
		var theFile;

		newFileName = newFileName ?? pathName;
		newFileName = newFileName.standardizePath;

		theFile = File(newFileName,"wb+");

		this.writeFile( theFile );
	}
}
BlofeldSoundBrowser {
	var <blofeld;
	var <window;
	var <header;
	var <body;
	var <categoriesMenu;
	var <soundsetsMenu;
	var <searchInput;
	var <keyboard;
	var <footer1, <footer2;
	var <currentSoundText;
	var <currentSoundNameText, <currentSoundSetText, <currentSoundLocationText, <currentSoundInfoText;
	var <currentSoundFilterEnv, <currentSoundAmpEnv;
	var <leftButton, <rightButton;
	var <sidebar;

	var <initialized = false;
	var <currentBank = \a;
	var <currentCategory = \all;
	var <currentSoundset;
	var <currentSounds;
	var <currentSound;
	var <currentPage = 1;
	var <numPages = 1;
	var <buttonArray;
	var <soundsArray;
	var <searchText = "";

	const windowWidth = 960;
	const windowHeight = 480;
	const sidebarWidth = 240;
	const gap = 5;
	const margin = 10;

	classvar <banks;
	classvar <categories;
	classvar <soundsets;
	classvar <numberOfRows = 10;
	classvar <numberOfColumns = 4;
	classvar <buttonsPerPage;
	classvar <buttonWidth;
	classvar <buttonHeight;
	classvar <backgrounds;
	classvar <soundsCache;

	*initClass {
		buttonsPerPage = (numberOfRows * numberOfColumns);
		buttonWidth = windowWidth - sidebarWidth - (margin * 2) - (numberOfColumns * (gap - 1)) / numberOfColumns;
		buttonHeight = 25; //(windowHeight * 0.75) / numberOfRows;
		backgrounds = (
			0: Color.white,
			1: Color.blue,
			2: Color.cyan,
			3: Color.gray,
			4: Color.green,
			5: Color.magenta,
			6: Color.red,
			7: Color.yellow,
		);
	}

	*updateClassvars {
		banks = [\all] ++ Blofeld.bank.keys.asArray.sort;
		categories = [\all] ++ Blofeld.category.values.sort.collect({ |v| Blofeld.category.findKeyForValue(v); });
		soundsets = [\all] ++ (BlofeldSoundset.files ++ BlofeldSoundset.loaded).keys.asArray.sort;
	}

	*new { |blofeld|
		^super.newCopyArgs(blofeld).init;
	}

	init { |loadAllSoundsets|
		currentSounds = [];

		BlofeldSoundBrowser.updateClassvars;

		this.createWindow;
		this.createHeader;
		this.createButtons;
		this.createFooter;
		this.createSidebar;
	}

	start { |loadSoundsets|
		this.updateCache;
		BlofeldSoundBrowser.updateClassvars;
		currentSoundset = if (BlofeldSoundset.loaded.size >= BlofeldSoundset.files.size, {
			\all;
		}, {
			loadSoundsets[0] ? BlofeldSoundset.loaded.keys.asArray.sort[0];
		});
		initialized = true;
		categoriesMenu.items = categories;
		soundsetsMenu.items = soundsets;
		currentSoundText.string = "click on a button to choose a Sound";
		// now that buttonArray exists, we can run EZPopUpMenu action to initialize button labels:
		categoriesMenu.valueAction = categories.indexOf(currentCategory);
		soundsetsMenu.valueAction = soundsets.indexOf(currentSoundset);
	}

	updateCache {
		var allSounds;
		allSounds = BlofeldSoundset.select(true);
		if (soundsCache.size != allSounds.size, {
			soundsCache = allSounds.sort {|a, b| a.getName < b.getName };
		});
	}

	createWindow {
		Window.closeAll;

		window = Window.new(
			name: "Blofeld sound browser",
			bounds: Rect.new(
				left: 100,
				top: 100,
				width: windowWidth,
				height: windowHeight
			),
			resizable: false
		);

		window.front;
	}

	createHeader {
		header = CompositeView.new(window, Rect.new(margin, margin, windowWidth - sidebarWidth - (margin * 2), 50));

		StaticText.new(
			parent: header,
			bounds: Rect(0, 0, header.bounds.width, header.bounds.height))
		.string_("Blofeld")
		// .background_(Color.green(0.5, 0.2))
		.align_(\topRight)
		.font_(Font(Font.default, size: 24, bold: true));

		StaticText.new(
			parent: header,
			bounds: Rect(0, 0, header.bounds.width, header.bounds.height - 10))
		.string_(if (blofeld.isConnected, { "connected" }, { "not connected" }))
		.align_(\bottomRight)
		.stringColor_(if (blofeld.isConnected, { Color.green }, { Color.red }));

		categoriesMenu = EZPopUpMenu.new(
			parentView: header,
			bounds: Rect.new(0, 10, 185, 30),
			label: "category: ",
			items: categories,
			globalAction: { |menu|
				currentCategory = menu.item;
				currentPage = 1;
				if (initialized, {
					this.reloadSounds;
					this.updatePages;
				});
			},
			initVal: if (currentCategory != \all, { categories.indexOf(currentCategory) }, { 0 }),
			initAction: false, // because buttonArray does not exist yet
			labelWidth: 60
		);

		soundsetsMenu = EZPopUpMenu.new(
			parentView: header,
			bounds: Rect.new(200, 10, 185, 30),
			label: "soundset: ",
			items: soundsets,
			globalAction: { |menu|
				var selected = menu.item.asSymbol;
				if (BlofeldSoundset.files[selected] != nil, {
					BlofeldSoundset.load(BlofeldSoundset.files[selected]);
					this.updateCache;
				});
				currentSoundset = selected;
				currentPage = 1;
				if (initialized, {
					this.reloadSounds;
					this.updatePages;
				});
			},
			initVal: if (currentSoundset != \all, { soundsets.indexOf(currentSoundset) }, { 0 }),
			initAction: false, // because buttonArray does not exist yet
			labelWidth: 60,
		);

		searchInput = TextField.new(header, Rect(400, 10, 185, 30))
		.string_(searchText).action_({
			searchText = searchInput.string;
			currentPage = 1;
			if (initialized, {
				this.reloadSounds;
				this.updatePages;
			});
		});
	}

	reloadSounds {
		var count = 0;
		var totalCount = 0;
		currentSounds = [];
		buttonArray.do({ |button|
			button.string = "";
			//button.background = nil;
		});
		soundsArray = Array.newClear(buttonsPerPage);
		currentSounds = soundsCache.select({ |sound|
			var accept = (sound[\category] == (if (currentCategory != \all, { Blofeld.category[currentCategory.asSymbol] }, { sound[\category] }))) &&
			(sound.soundset.name == (if (currentSoundset != \all, { currentSoundset.asSymbol }, { sound.soundset.name }))) &&
			(if (searchText.size > 0, { sound.getName().containsi(searchText) }, { true }));
			if (accept, {
				if ((totalCount >= (buttonsPerPage*(currentPage-1))) && (count < (buttonsPerPage*currentPage)), {
					count = count + 1;
					totalCount = totalCount + 1;
					accept;
				}, {
					totalCount = totalCount + 1;
					false;
				});
			}, {
				false;
			});
		}).sort({ |a, b|
			a.getName() < b.getName();
		});
		(buttonsPerPage.min(currentSounds.size)).do({|i|
			var sound = currentSounds[i];
			var indexDownByColumn = i % numberOfRows * numberOfColumns + i.div(numberOfRows);
			buttonArray[indexDownByColumn].string = sound.getName();
			buttonArray[indexDownByColumn].background = backgrounds[sound[\category]];
			soundsArray[indexDownByColumn] = sound;
		});
		numPages = (totalCount / buttonsPerPage).ceil.asInteger;
	}

	updatePages {
		leftButton.string = "<<  % / %".format(currentPage, numPages);
		rightButton.string = "% / %  >>".format(currentPage, numPages);
	}

	createButtons {
		body = CompositeView.new(window, Rect.new(0, 50, windowWidth - sidebarWidth, 315));

		body.decorator = FlowLayout(body.bounds, Point(margin, margin), Point(gap, gap));

		buttonArray = buttonsPerPage.collect({ |i|
			Button.new(
				parent: body,
				bounds: Point.new(buttonWidth, buttonHeight),
			)
			.action_(this.onClickButton);
		});
	}

	onClickButton {
		^{ |button|
			if (button.string.size > 0, {
				currentSound = soundsArray[buttonArray.indexOf(button)];
				this.updateSound;
			});
		}
	}

	updateSound {
		var filterEnv, ampEnv;
		currentSoundText.string = currentSound.getName();
		currentSoundNameText.string = currentSound.getName();
		currentSoundSetText.string = currentSound.soundset.name.asString;
		currentSoundLocationText.string = Blofeld.bank.findKeyForValue(currentSound.bank).asString.toUpper ++ (currentSound.program+1).asString.padLeft(3, "0") + ", category:" + currentSound[\category];

		filterEnv = currentSound.getEnv("filterEnv") / 127.0;
		filterEnv[0][1] = filterEnv[0][1]/3;
		filterEnv[0][2] = filterEnv[0][2]/3 + (1/3);
		filterEnv[0][3] = filterEnv[0][3]/3 + (2/3);
		currentSoundFilterEnv.value = filterEnv;

		ampEnv = currentSound.getEnv("ampEnv") / 127.0;
		ampEnv[0][1] = ampEnv[0][1]/3;
		ampEnv[0][2] = ampEnv[0][2]/3 + (1/3);
		ampEnv[0][3] = ampEnv[0][3]/3 + (2/3);
		currentSoundAmpEnv.value = ampEnv;

		currentSoundInfoText.string = currentSound.getInfo();
		if (blofeld.isConnected, {
			blofeld.editBuffer.upload(currentSound);
		});
	}

	createFooter {
		footer1 = CompositeView.new(window, Rect.new(margin, 360, windowWidth - sidebarWidth - (margin * 2), 50));

		currentSoundText = StaticText.new(
			parent: footer1,
			bounds: Rect(0, 0, footer1.bounds.width, footer1.bounds.height))
		.string_("loading sounds...")
		.background_(Color.gray(0.5, 0.2))
		.align_(\center)
		.font_(Font(Font.default, size: 24, bold: true))
		.front;

		footer2 = CompositeView.new(window, Rect.new(margin, 415 + gap, windowWidth - sidebarWidth - (margin * 2), 50));

		// placeholder button
		leftButton = Button.new(
			parent: footer2,
			bounds: Rect.new(
				left: 0,
				top: 0,
				width: buttonWidth,
				height: 50
			)
		)
		.string_("<<  % / %".format(currentPage, numPages))
		// .font_(Font(Font.default.name, 18))
		.action_({ |button|
			currentPage = (currentPage - 1).max(1);
			this.reloadSounds;
			this.updatePages;
		});

		// keyboard
		if (Quarks.isInstalled("ixiViews")) {
			keyboard = MIDIKeyboard.new(footer2, Rect.new(
				left: buttonWidth + gap,
				top: 0,
				width: footer2.bounds.width - (buttonWidth*2) - (gap*2),
				height: 50
			));
			keyboard.keyDownAction_({ |note|
				blofeld.midiOut.noteOn(0, note, 60);
			});
			keyboard.keyUpAction_({ |note|
				blofeld.midiOut.noteOff(0, note, 60);
			});
			keyboard.setColor(60, Color.red);
		};

		rightButton = Button.new(
			parent: footer2,
			bounds: Rect.new(
				left: footer2.bounds.width - buttonWidth,
				top: 0,
				width: buttonWidth,
				height: 50
			)
		)
		.string_("% / %  >>".format(currentPage, numPages))
		// .font_(Font(Font.default.name, 18))
		.action_({ |button|
			currentPage = (currentPage + 1).min(numPages);
			this.reloadSounds;
			this.updatePages;
		});
	}

	createSidebar {
		sidebar = CompositeView.new(window, Rect.new(windowWidth - sidebarWidth, 0, sidebarWidth, windowHeight));

		currentSoundNameText = StaticText.new(sidebar, Rect(0, 0, sidebar.bounds.width, 16))
		.font_(Font(Font.default, size: 14, bold: true))
		.string_("");

		currentSoundSetText = StaticText.new(sidebar, Rect(0, 16, sidebar.bounds.width, 12))
		.font_(Font(Font.default, size: 11, bold: false))
		.string_("");

		currentSoundLocationText = StaticText.new(sidebar, Rect(0, 28, sidebar.bounds.width, 12))
		.font_(Font(Font.default, size: 11, bold: false))
		.string_("");

		StaticText(sidebar, Rect(0, 40, sidebar.bounds.width, 12))
		.font_(Font(Font.default, size: 11, bold: false))
		.string_("Filter Envelope");
		currentSoundFilterEnv = EnvelopeView.new(sidebar, Rect(0, 52, sidebar.bounds.width, 50));

		StaticText(sidebar, Rect(0, 102, sidebar.bounds.width, 12))
		.font_(Font(Font.default, size: 11, bold: false))
		.string_("Amp Envelope");
		currentSoundAmpEnv = EnvelopeView.new(sidebar, Rect(0, 114, sidebar.bounds.width, 50));

		currentSoundInfoText = StaticText.new(
			parent: ScrollView.new(sidebar, Rect.new(0, sidebar.bounds.height / 2, sidebar.bounds.width, sidebar.bounds.height / 2)).hasHorizontalScroller_(false),
			bounds: Rect(0, 0, sidebar.bounds.width, windowHeight))
		.string_("")
		.background_(Color.gray(0.5, 0.2))
		.align_(\topLeft)
		.font_(Font(Font.default, size: 11, bold: false));
	}
}

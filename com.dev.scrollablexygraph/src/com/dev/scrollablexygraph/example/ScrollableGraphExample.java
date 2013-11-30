package com.dev.scrollablexygraph.example;

/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.dev.scrollablexygraph.ScrollableGraphComposite;

public class ScrollableGraphExample {

	public static void main(String[] args) {
		final Shell shell = new Shell();
		shell.setSize(600, 400);
		GridLayoutFactory.fillDefaults().applyTo(shell);

		createGraphArea(shell);
		shell.open();

		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}

	private static void createGraphArea(Shell shell) {
		 new ScrollableGraphComposite(shell, SWT.NONE);
	}

}

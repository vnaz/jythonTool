import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.InteractiveConsole;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.List;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        final InteractiveConsole interp = new InteractiveConsole();

        if (!GraphicsEnvironment.isHeadless()) {
            JFrame frame = new JFrame();

            final ConsoleTextArea console = new ConsoleTextArea() {
                boolean more = false;

                final String ps1 = ">>> ";
                final String ps2 = "... ";

                @Override
                public void appendPrompt() {
                    if (more) {
                        appendImpl(ps2, STYLE_PROMPT);
                    } else {
                        appendImpl(ps1, STYLE_PROMPT);
                    }
                }

                @Override
                protected String getSyntaxStyle() {
                    return SyntaxConstants.SYNTAX_STYLE_PYTHON;
                }

                @Override
                protected String getUsageNote() {
                    return "";
                }

                @Override
                protected void handleSubmit(String text) {
                    // in.push(text + "\n");
                    // appendImpl("", STYLE_STDOUT, true);

                    more = interp.push(text);
                    appendPrompt();

//                    final String line = text;
//                    new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                more = interp.push(line);
//                                appendPrompt();
//                            }
//                        }).start();
                }

                @Override
                protected void init() {
                    append(interp.getDefaultBanner(), STYLE_STDOUT);

                    OutputStream out = new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            appendImpl(Character.toString((char) b), ConsoleTextArea.STYLE_STDOUT);
                        }
                    };

                    interp.setOut(out);
                    interp.setErr(out);

                    try {
                        String appPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                        String jythonPath = appPath + "/jython";

                        interp.getSystemState().path.append(new PyString(jythonPath));
                        interp.execfile(jythonPath + "/startup.py");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    appendPrompt();
                }
            };

            console.setPreferredSize(new Dimension(600,400));
            JScrollPane scrollPane = new JScrollPane(console);
            frame.setContentPane(scrollPane);
            frame.setTitle("Console");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            final DefaultCompletionProvider completionProvider = new DefaultCompletionProvider();
            AutoCompletion autoCompletion = new AutoCompletion(completionProvider);
            autoCompletion.install(console);
            autoCompletion.addAutoCompletionListener(new AutoCompletionListener() {
                @Override
                public void autoCompleteUpdate(AutoCompletionEvent autoCompletionEvent) {
                    completionProvider.addCompletion(new BasicCompletion(completionProvider, "Listener"));
                }
            });
            completionProvider.addCompletion(new BasicCompletion(completionProvider, "Test"));

        } else {
            interp.interact();
        }
    }
}

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.python.util.InteractiveConsole;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Main {

    public static class InStream extends InputStream {
        private String data = "";
        private int pos = 0;

        public synchronized int available() throws IOException {
            return data.length();
        }

        public synchronized int read() throws IOException {

            if (pos < data.length()) {
                int x = data.charAt(pos++);
                return x;
            }

            try {
                this.wait();
                return read();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return -1;
        }

        public synchronized void push(String new_data) {
            data = data.substring(pos) + new_data;
            pos = 0;
            this.notify();
        }
    };

    public static void main(String[] args) throws IOException {
        final InteractiveConsole interp = new InteractiveConsole();

        if (!GraphicsEnvironment.isHeadless()) {
            JFrame frame = new JFrame();

//            final InStream in = new InStream();
//            interp.setIn(in);

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
                    return "Usage notes call";
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

            OutputStream out = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    console.appendImpl(Character.toString((char) b), ConsoleTextArea.STYLE_STDOUT);
                }
            };

            interp.setOut(out);
            interp.setErr(out);
        } else {
            interp.interact();
        }
    }
}

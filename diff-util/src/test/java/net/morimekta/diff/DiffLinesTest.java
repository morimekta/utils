package net.morimekta.diff;

import net.morimekta.util.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DiffLinesTest {
    @Test
    public void testDiffLines_code_move() throws IOException {
        String code1 = IOUtils.readString(getClass().getResourceAsStream("/Change1.java"));
        String code2 = IOUtils.readString(getClass().getResourceAsStream("/Change2.java"));

        DiffLines diff = new DiffLines(code1, code2);

        assertThat(diff.patch(),
                   is("@@ -51,8 +51,0 @@\n" +
                      "-     * Display a human-readable version of this DiffBase.\n" +
                      "-     * @return text version.\n" +
                      "-     */\n" +
                      "-    public String toString() {\n" +
                      "-        return \"Change(\" + this.operation + \",\\\"\" + Strings.escape(this.text) + \"\\\")\";\n" +
                      "-    }\n-" +
                      "\n" +
                      "-    /**\n" +
                      "@@ -84,0 +76,9 @@\n" +
                      "+    }\n" +
                      "+\n" +
                      "+    /**\n" +
                      "+     * Display a human-readable version of this DiffBase.\n" +
                      "+     * @return text version.\n" +
                      "+     */\n" +
                      "+    @Override\n" +
                      "+    public String toString() {\n" +
                      "+        return \"Change(\" + this.operation + \",\\\"\" + Strings.escape(this.text) + \"\\\")\";\n" +
                      ""));
    }

    @Test
    public void testDiffLines_longlines() {
        String lorem1 =
                "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Proin nibh augue,\n" +
                "suscipit a, scelerisque sed, lacinia in, mi. Cras vel lorem. Etiam pellentesque\n" +
                "aliquet tellus. Phasellus pharetra nulla ac diam. Quisque semper justo at\n" +
                "risus. Donec venenatis, turpis vel hendrerit interdum, dui ligula ultricies\n" +
                "purus, sed posuere libero dui id orci. Nam congue, pede vitae dapibus aliquet,\n" +
                "elit magna vulputate arcu, vel tempus metus leo non est. Etiam sit amet lectus\n" +
                "quis est congue mollis. Phasellus congue lacus eget neque. Phasellus ornare,\n" +
                "ante vitae consectetuer consequat, purus sapien ultricies dolor, et mollis pede\n" +
                "metus eget nisi. Praesent sodales velit quis augue. Cras suscipit, urna at\n" +
                "aliquam rhoncus, urna quam viverra nisi, in interdum massa nibh nec erat.\n";

        String lorem2 =
                "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Proin nibh augue,\n" +
                "aliquet tellus. Phasellus pharetra nulla ac diam. Quisque semper justo at\n" +
                "risus. Donec venenatis, turpis vel hendrerit interdum, dui ligula ultricies\n" +
                "purus, sed posuere libero dui id orci. Nam congue, pede vitae dapibus aliquet,\n" +
                "elit magna vulputate arcu, vel tempus metus leo non est. Etiam sit amet lectus\n" +
                "quis est congue mollis. Phasellus kongue lacus eget neque. Phasellus ornare,\n" +
                "suscipit a, scelerisque sed, lacinia in, mi. Cras vel lorem. Etiam pellentesque\n" +
                "ante vitae consectetuer consequat, purus sapien ultricies dolor, et mollis pede\n" +
                "metus eget nisi. Praesent sodales velit quis augue. Cras suscipit, urna at\n" +
                "aliquam rhoncus, urna quam viverra nisi, in interdum massa nibh nec erat.\n";

        DiffLines diff = new DiffLines(lorem1, lorem2);

        assertThat(diff.patch(),
                   is("@@ -2,1 +2,0 @@\n" +
                      "-suscipit a, scelerisque sed, lacinia in, mi. Cras vel lorem. Etiam pellentesque\n" +
                      "@@ -7,1 +6,2 @@\n" +
                      "-quis est congue mollis. Phasellus congue lacus eget neque. Phasellus ornare,\n" +
                      "+quis est congue mollis. Phasellus kongue lacus eget neque. Phasellus ornare,\n" +
                      "+suscipit a, scelerisque sed, lacinia in, mi. Cras vel lorem. Etiam pellentesque\n"));
        assertThat(diff.fullDiff(),
                   is(" Lorem ipsum dolor sit amet, consectetur adipisicing elit. Proin nibh augue,\n" +
                      "-suscipit a, scelerisque sed, lacinia in, mi. Cras vel lorem. Etiam pellentesque\n" +
                      " aliquet tellus. Phasellus pharetra nulla ac diam. Quisque semper justo at\n" +
                      " risus. Donec venenatis, turpis vel hendrerit interdum, dui ligula ultricies\n" +
                      " purus, sed posuere libero dui id orci. Nam congue, pede vitae dapibus aliquet,\n" +
                      " elit magna vulputate arcu, vel tempus metus leo non est. Etiam sit amet lectus\n" +
                      "-quis est congue mollis. Phasellus congue lacus eget neque. Phasellus ornare,\n" +
                      "+quis est congue mollis. Phasellus kongue lacus eget neque. Phasellus ornare,\n" +
                      "+suscipit a, scelerisque sed, lacinia in, mi. Cras vel lorem. Etiam pellentesque\n" +
                      " ante vitae consectetuer consequat, purus sapien ultricies dolor, et mollis pede\n" +
                      " metus eget nisi. Praesent sodales velit quis augue. Cras suscipit, urna at\n" +
                      " aliquam rhoncus, urna quam viverra nisi, in interdum massa nibh nec erat.\n"));
    }
}

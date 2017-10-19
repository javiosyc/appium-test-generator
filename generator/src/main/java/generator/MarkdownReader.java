package generator;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.vladsch.flexmark.ast.Block;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.VisitHandler;
import com.vladsch.flexmark.ast.Visitor;
import com.vladsch.flexmark.ast.util.TextCollectingVisitor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class MarkdownReader {

	private String filePath;

	private MutableDataSet options = new MutableDataSet();

	public MarkdownReader(String filePath) {
		this.filePath = filePath;
	}

	public void parse() throws IOException {
		Parser parser = Parser.builder(options).build();

		Reader reader = new FileReader(filePath);
		Node document = parser.parseReader(reader);

		ReversiblePeekingIterator<Node> nodes = document.getChildIterator();

		nodes.forEachRemaining(node -> {

			if (node instanceof Heading) {

				Heading block = (Heading) node;

				int level = block.getLevel();
				if (level == 2) {
					System.out.println(block.getLevel());
					System.out.println(block.getText());

					BasedSequence seq = block.getText();

					String url = StringUtils.substringBetween(seq.toString(), "[", "]");

					String[] urls = StringUtils.split(url, "/");

					System.out.println(url);

					String serviceMethod = urls[0];
					String methodMethod = urls[1];

					System.out.println(serviceMethod);
					System.out.println(methodMethod);
				}
			}

		});
	}

	public static void main(String[] args) throws IOException {

		MarkdownReader markdownReader = new MarkdownReader("neo-api.md");

		markdownReader.parse();
	}

}

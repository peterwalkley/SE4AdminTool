package tfa.se4.gui;

import javafx.scene.control.ListCell;
import org.apache.commons.lang3.StringUtils;
import tfa.se4.LogColours;
import tfa.se4.logger.LoggerInterface;

import java.util.List;

public class ColouredCell extends ListCell<String>
{
    private final LoggerInterface mLogger;

    public ColouredCell(final LoggerInterface logger)
    {
        mLogger = logger;
    }

    @Override
    public void updateItem(String text, boolean empty)
    {
        super.updateItem(text, empty);

        if (StringUtils.isBlank(text) || empty)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            if (text.indexOf((char)1) != -1)
            {
                setTextFill(LogColours.getDefaultChat(mLogger));
                setText(StringUtils.remove(text, (char)1));
                return;

            }
            setTextFill(LogColours.getDefault(mLogger));
            setText(text);
            final List<LogColours.LogColour> colourMatches = LogColours.getColours(mLogger);

            for (final LogColours.LogColour toMatch : colourMatches)
            {
                if (toMatch.pattern.matcher(text).find())
                {
                    setTextFill(toMatch.colour);
                    return;
                }
            }
        }
    }
}
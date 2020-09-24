package tfa.se4.gui;

import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import tfa.se4.LogColours;
import tfa.se4.logger.LoggerInterface;

import java.util.List;
import java.util.logging.Logger;

public class ColouredCell extends ListCell<String>
{
    private LoggerInterface m_logger;

    public ColouredCell(final LoggerInterface logger)
    {
        m_logger = logger;
    }

    @Override
    public void updateItem(String text, boolean empty)
    {
        super.updateItem(text, empty);
        this.setTextFill(LogColours.getDefault(m_logger));

        if (StringUtils.isBlank(text) || empty)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            if (text.indexOf((char)1) != -1)
            {
                setTextFill(LogColours.getDefaultChat(m_logger));
                setText(StringUtils.remove(text, (char)1));
                return;

            }
            setText(text);
            final List<LogColours.LogColour> colourMatches = LogColours.getColours(m_logger);

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
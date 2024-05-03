package de.uni_trier.wi2.parsing.model;

import de.uni_trier.wi2.procake.data.object.DataObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexComponent implements ConditionComponent {
    final private StringOrMethodReturnValueComponent pattern, string;

    public RegexComponent(StringOrMethodReturnValueComponent pattern, StringOrMethodReturnValueComponent string) {
        this.pattern = pattern;
        this.string = string;
    }

    @Override
    public Boolean evaluate(DataObject q, DataObject c) {
        Pattern p = Pattern.compile((String) pattern.evaluate(q,c));
        Matcher matcher = p.matcher((String) string.evaluate(q,c));
        return matcher.matches();
    }
}

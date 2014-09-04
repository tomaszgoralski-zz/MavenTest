import javax.xml.bind.annotation.*;

@XmlRootElement(name = "adage")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"words", "wordCount"})
public class Adage {
    @XmlElement(required = true)
    protected String words;
    @XmlElement(required = true)
    protected int wordCount;

    public Adage() {
    }

    @Override
    public String toString() {
        return words + " -- " + wordCount + " words";
    }

    // properties
    public void setWords(String words) {
        this.words = words;
        this.wordCount = words.trim().split("\\s+").length;
    }

    public String getWords() {
        return this.words;
    }

    public void setWordCount(int wordCount) {
    }

    public int getWordCount() {
        return this.wordCount;
    }
}

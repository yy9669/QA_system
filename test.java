package src;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.tagging.PartOfSpeechTagging;

import java.util.List;

public class test {
    public static void main(String[] var0) {
        List<Word>var1 = WordSegmenter.segWithStopWords("我爱中国，我爱杨尚川");
        System.out.println("未标注词性：" + var1);
        PartOfSpeechTagging.process(var1);
        Word a= var1.get(2);
        System.out.println(a.getText());
        System.out.println(a.getPartOfSpeech().getDes());
        System.out.println(a.getPartOfSpeech().getPos());
    }
}

package at.tugraz.asd.LANG.Controller;


import at.tugraz.asd.LANG.Exeptions.EditFail;
import at.tugraz.asd.LANG.Languages;
import at.tugraz.asd.LANG.Messages.in.CreateVocabularyMessageIn;
import at.tugraz.asd.LANG.Messages.in.EditVocabularyMessageIn;
import at.tugraz.asd.LANG.Messages.out.TranslationOut;
import at.tugraz.asd.LANG.Messages.out.VocabularyLanguageOut;
import at.tugraz.asd.LANG.Messages.out.VocabularyOut;
import at.tugraz.asd.LANG.Model.VocabularyModel;
import at.tugraz.asd.LANG.Service.VocabularyService;
import org.apache.logging.log4j.util.PropertySource;
import org.hibernate.usertype.UserVersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    @Autowired
    VocabularyService service;

    @PostMapping
    public ResponseEntity addVocabulary(@RequestBody CreateVocabularyMessageIn msg){
        service.saveVocabulary(msg);
        return ResponseEntity.ok(null);
    }

    @GetMapping (path = "/topics")
    @ResponseBody
    public ResponseEntity getAllTopics()
    {
        return ResponseEntity.ok(service.getAllTopics());
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity getAllVocabulary(){
        ArrayList<VocabularyOut> ret = new ArrayList<>();
        List<VocabularyModel> vocab = service.getAllVocabulary();
        if(vocab.isEmpty())
            return ResponseEntity.noContent().build();
        vocab.forEach(el->{
            HashMap<Languages, String> translation = new HashMap<>();
            el.getTranslationVocabMapping().forEach(translationModel -> {
                translation.put(translationModel.getLanguage(), translationModel.getVocabulary());
            });
            ret.add(new VocabularyOut(
                    el.getTopic(),
                    el.getVocabulary(),
                    translation,
                    el.getRating()
            ));
        });
        return ResponseEntity.ok(ret);
    }

    @GetMapping (path = "{Language}")
    public ResponseEntity getAllVocabularyOfLanguage(@PathVariable("Language") Languages language)
    {
        VocabularyLanguageOut ret = new VocabularyLanguageOut(service.getAllVocabularyOfLanguage(language));
        return ResponseEntity.ok(ret);
    }

    @GetMapping (path = "{Language}/{word}")
    public ResponseEntity getTranslation(@PathVariable("Language") Languages language, @PathVariable("word") String word)
    {
        TranslationOut ret = new TranslationOut(service.getTranslation(language, word));
        return ResponseEntity.ok(ret);
    }
    @PutMapping
    @ResponseBody
    public ResponseEntity editVocabulary(@RequestBody EditVocabularyMessageIn msg){
        try{
            service.editVocabulary(msg);
            return ResponseEntity.ok(null);
        }
        catch (EditFail e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping (path = "alphabetically/{aORz}")
    @ResponseBody
    public ResponseEntity getAllVocabularyAlphabetically1(@PathVariable("aORz")String aOrz){
        ArrayList<VocabularyOut> ret = new ArrayList<>();
        List<VocabularyModel> vocab = service.sortVocabOverview(aOrz);
        if(vocab.isEmpty())
            return ResponseEntity.noContent().build();

        vocab.forEach(el->{
            HashMap<Languages, String> translation = new HashMap<>();
            el.getTranslationVocabMapping().forEach(translationModel -> {
                translation.put(translationModel.getLanguage(), translationModel.getVocabulary());
            });
            ret.add(new VocabularyOut(
                    el.getTopic(),
                    el.getVocabulary(),
                    translation,
                    el.getRating()
            ));
        });
        return ResponseEntity.ok(ret);
    }

    @GetMapping (path = "alphabetically/{Language}/{aORz}")
    @ResponseBody
    public ResponseEntity getAllVocabularyAlphabetically2(@PathVariable("Language")Languages language, @PathVariable("aORz")String aOrz){
        ArrayList<VocabularyOut> ret = new ArrayList<>();
        List<VocabularyModel> vocab = service.sortVocabStudyInterface(language,aOrz);
        if(vocab.isEmpty())
            return ResponseEntity.noContent().build();

        vocab.forEach(el->{
            HashMap<Languages, String> translation = new HashMap<>();
            el.getTranslationVocabMapping().forEach(translationModel -> {
                translation.put(translationModel.getLanguage(), translationModel.getVocabulary());
            });
            ret.add(new VocabularyOut(
                    el.getTopic(),
                    el.getVocabulary(),
                    translation,
                    el.getRating()
            ));
        });
        return ResponseEntity.ok(ret);
    }

    @GetMapping (path = "random")
    @ResponseBody
    public ResponseEntity getRandomVocabulary() {
        int testSize = 10;      // change if test size varies in future issues
        ArrayList<VocabularyOut> ret = new ArrayList<>();
        List<VocabularyModel> randomVocab = new ArrayList<>();
        Random rand = new Random();

        List<VocabularyModel> vocab = service.getAllVocabulary();

        // Check if vocab list exists and has enough vocabs
        if(vocab.isEmpty() || vocab.size() < testSize)
            return ResponseEntity.noContent().build();

        // Select x amount of random vocabs from vocab list, and remove element from vocab list to avoid duplicates
        for (int i = 0; i < testSize; i++) {
            VocabularyModel randomVocabItem = vocab.get(rand.nextInt(vocab.size()));
            randomVocab.add(randomVocabItem);
            vocab.remove(randomVocabItem);
        }
        System.out.println("Random Vocabs are: " + randomVocab.toString());

        // Build response
        randomVocab.forEach(el->{
            HashMap<Languages, String> translation = new HashMap<>();
            el.getTranslationVocabMapping().forEach(
                    translationModel -> translation.put(translationModel.getLanguage(), translationModel.getVocabulary())
            );
            ret.add(new VocabularyOut(
                    el.getTopic(),
                    el.getVocabulary(),
                    translation,
                    el.getRating()

            ));
        });

        return ResponseEntity.ok(ret);
    }
}

# -injars build/libs/lucenestudy.jar
# -outjars slimmed.jar
-libraryjars <java.home>/lib/rt.jar

# -dontshrink
-dontoptimize
-dontobfuscate
# -dontpreverify
# -printusage deadcode.txt
# -printmapping mapping.txt
# -printseeds seeds.txt

-dontwarn org.apache.regexp.**
-dontwarn org.apache.lucene.search.grouping.GroupDocs
-dontwarn org.apache.lucene.search.grouping.TopGroups

-keepattributes Signature,Exceptions

# -keep public class org.lukhnos.lucenestudy.Study {
#    *** main(***);
# }

# -keep class org.lukhnos.lucenestudy.*

-keep,includedescriptorclasses class org.lukhnos.lucenestudy.* {
    *;
}

# -keepclassmembers class org.lukhnos.** {
#     *;
# }

-keep class org.apache.lucene.codecs.**

-keep,includedescriptorclasses class **.*AttributeImpl {
    *;
}

-keep,includedescriptorclasses class org.apache.lucene.analysis.* {
    *;
}

# -keepclassmembers class org.apache.lucene.core.** {
# -keep class org.apache.lucene.core.** {
-keepclasseswithmembers class org.apache.lucene.core.** {
    boolean incrementToken();
}

# -keepclassmembers class org.apache.lucene.analysis.* {
# -keepclasseswithmembers class org.apache.lucene.analysis.* {
#     boolean incrementToken();
# }

# -keepclassmembers class org.apache.lucene.analysis.en.* {
-keepclasseswithmembers class org.apache.lucene.analysis.en.* {
    boolean incrementToken();
}

-keepclasseswithmembers class ** {
# -keep class ** {
    void cleaner();
    void clean();
    *** getValue();
}

-dontnote org.apache.lucene.analysis.synonym.SynonymFilterFactory
-dontnote org.apache.lucene.analysis.util.AnalysisSPILoader
-dontnote org.apache.lucene.store.LockStressTest
-dontnote org.apache.lucene.util.CommandLineUtil
-dontnote org.apache.lucene.util.RamUsageEstimator

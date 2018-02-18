debug:
	ant debug

clean:
	ant clean

test:
	adb shell am instrument -w \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner

install:
	adb install -r bin/Test-debug.apk

uninstall:
	adb uninstall eu.lavarde.spacetasker.test

makeinstallmother:
	cd ../NoNonsenseNotes/;make debug install

#For class: -Dspoon.test.class=fully.qualified.ClassName
#For method in that class, also: -Dspoon.test.method=testAllTheThings
testspoon:
	java -jar spoon-runner-1.0.2-jar-with-dependencies.jar \
--apk ../NoNonsenseNotes/bin/NotePad-debug.apk \
--test-apk bin/Test-debug.apk \
--sdk ~/android-sdk-linux

testeditor:
	adb shell am instrument -w \
-e class eu.lavarde.spacetasker.test.FragmentTaskDetailTest \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner

testlist:
	adb shell am instrument -w \
-e class eu.lavarde.spacetasker.test.FragmentTaskListsTest \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner

testlistpager:
	adb shell am instrument -w \
-e class eu.lavarde.spacetasker.test.FragmentTaskListsViewPagerTest \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner


testprovider:
	adb shell am instrument -w \
-e class eu.lavarde.spacetasker.test.DBProviderTest \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner


testgtask:
	adb shell am instrument -w \
-e class eu.lavarde.spacetasker.test.GTaskSyncTest \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner

testrfc:
	adb shell am instrument -w \
-e class eu.lavarde.spacetasker.test.RFCDateTest \
eu.lavarde.spacetasker.test/android.test.InstrumentationTestRunner

package org.diffenbach.enumradiogroup.test;

import java.util.Arrays;
import java.util.EnumSet;

import org.diffenbach.android.widgets.EnumRadioGroup;
import org.diffenbach.enumradiogroup.MainActivity;
import org.diffenbach.enumradiogroup.MainActivity.Agreed;
import org.diffenbach.enumradiogroup.MainActivity.Pet;
import org.diffenbach.enumradiogroup.MainActivity.Pie;
import org.diffenbach.enumradiogroup.MainActivity.Sex;
import org.diffenbach.enumradiogroup.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.Checkable;
import android.widget.RadioButton;

import com.robotium.solo.Solo;


public class PhoneTest extends ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;
	
	MainActivity mainActivity;
	EnumRadioGroup<?> egrs[] = new EnumRadioGroup<?>[5];
	EnumRadioGroup<Agreed> xmlAgreed;
	EnumRadioGroup<Sex> xmlSex;
	EnumRadioGroup<Agreed> pAgreed;
	EnumRadioGroup<Pie> pPies;
	EnumRadioGroup<Pet> pPets;
	Enum<?> defaults[] = new Enum<?>[]{Agreed.NO, Sex.REQUIRED_FIELD, Agreed.NO, Pie.POTATO, Pet.NONE};
	Enum<?> values[][] = {Agreed.values(), Sex.values(), Agreed.values(), Pie.values(), Pet.values()};
	
	public PhoneTest() {
		super(MainActivity.class);
	}

	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation());
		mainActivity = getActivity();
		
		// Wait for views:
		egrs[0] = xmlAgreed = (EnumRadioGroup<Agreed>) solo.getView(R.id.agreed1);
		egrs[1] = xmlSex = (EnumRadioGroup<Sex>) solo.getView(R.id.sex);
		egrs[2] = pAgreed = (EnumRadioGroup<Agreed>) solo.getView(MainActivity.P_AGREED_ID);
		egrs[3] = pPies = (EnumRadioGroup<Pie>) solo.getView(MainActivity.P_PIES_ID);
		egrs[4] = pPets = (EnumRadioGroup<Pet>) solo.getView(MainActivity.P_PETS_ID);
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	
	public void testViewsArePresent() {

		int timeout = 5; 
		
		// Wait for activity: 
		assertTrue("MainActivity not found", 
				solo.waitForActivity(org.diffenbach.enumradiogroup.MainActivity.class, 2000));
		
		// Wait for views:
		assertTrue("XML agreed not found", solo.waitForView(R.id.agreed1));
		assertTrue("XML sex not found", solo.waitForView(R.id.sex));
		assertTrue("Programmatic agreed not found", solo.waitForView(MainActivity.P_AGREED_ID));
		assertTrue("Programmatic pies not found", solo.waitForView(MainActivity.P_PIES_ID));
		assertTrue("Programmatic pies not found", solo.waitForView(MainActivity.P_PETS_ID));
	}
	
	public void testDefaultsAreCorrectAndChecked() {
		int offset = 0 ;
		for( EnumRadioGroup<?> egr : egrs) {
			Enum<?> v = defaults[offset++];
			verifyDefaultValue(egr, v);
			verifyCheckedValue(egr, v);
			verifyCheckedValueIsChecked(egr);
		}
	}
		
	public void testAllEnumsHaveACorrspondingRadioButton() {
		for( EnumRadioGroup<?> egr : egrs) {
			verifyAllRadioButtonsExist(egr);
		}
	}
	
	public void testValuesArrayIsIdenticalToEnumValues() {
		int offset = 0;
		for( EnumRadioGroup<?> egr : egrs) {
			verifyValuesArrayIsCorrect(egr, values[offset++]);
		}
	}
	
	public void testRadioButtonVFilteringIsAsSpecified() {
		verifyRadioButtonsHidden(xmlAgreed, EnumSet.noneOf(Agreed.class));
		verifyRadioButtonsHidden(xmlSex, EnumSet.of(Sex.REQUIRED_FIELD));
		verifyRadioButtonsHidden(pAgreed, EnumSet.of(Agreed.NO));
		verifyRadioButtonsHidden(pPies, EnumSet.of(Pie.APPLE));
		verifyRadioButtonsHidden(pPets, EnumSet.noneOf(Pet.class));
	}
	
	@UiThreadTest
	public void testRadioButtonIdsAreContiguousAndAPIsAreConsistent() {
		for( EnumRadioGroup<?> egr : egrs) {
			verifyContiguousAndConsistentWithRadioButtonsGroupAPI(egr);
		}
	}
	
	private void verifyDefaultValue(EnumRadioGroup<?> g, Enum<?> v) {
		assertTrue("Default is not correct " + v, g.getDefault() == v);
	}
	
	private void verifyCheckedValue(EnumRadioGroup<?> g, Enum<?> v) {
		assertTrue("getCheckedValue is not " + v, g.getCheckedValue() == v);
	}
	
	private void verifyCheckedValueIsChecked(EnumRadioGroup<?> g) {
		assertTrue("Checked value is not actually checked " + g.getCheckedValue().toString(),
				((RadioButton) solo.getView(g.getCheckedRadioButtonId()) ).isChecked());
	}
	
	private <T extends Enum<T>> void verifyAllRadioButtonsExist(EnumRadioGroup<T> g) {
		for(T ec : g.values()) {
			int id = g.getViewIdForEnum(ec);
			View rb = solo.getView(id);
			assertTrue("Radio button does not exist", rb != null);
			assertTrue("Radio button has wrong parent", rb.getParent() == g);
			assertTrue("Radio button doesn't match findViewByEnum", rb == g.findViewByEnum(ec));
		}
	}
	
	private void verifyValuesArrayIsCorrect(EnumRadioGroup<?> g, Enum<?>[] a ) {
		assertTrue("Values arrays are unequal "
				+ Arrays.asList(g.values())  + " " + Arrays.asList(a),
				Arrays.equals(g.values(), a));
	}
	
	private <T extends Enum<T>> void doVerifyRadioButtonVisibility(EnumRadioGroup<T> g, 
			EnumSet<T> gone, int visibility) {
		for( T ec: gone ) {
			assertTrue( "Wrong visibility " + ec, 
					g.findViewByEnum(ec).getVisibility() == visibility);
		}
	}
	
	private <T extends Enum<T>> void verifyRadioButtonsHidden(EnumRadioGroup<T> g, EnumSet<T> gone) {
		doVerifyRadioButtonVisibility(g, gone, View.GONE);
		doVerifyRadioButtonVisibility(g, EnumSet.complementOf(gone), View.VISIBLE);
	}
	
	private <T extends Enum<T>> void verifyContiguousAndConsistentWithRadioButtonsGroupAPI(EnumRadioGroup<T> g) {
		T[] values = g.values();
		int[] rbIds = new int[values.length];
		int offset = 0; 
		
		//check with values
		for(T ec : values) {
			int id;
			// raises CalledFromWromgThreadException! if @UITest not applied
			g.check(ec); // check using EGR API
			id = g.getCheckedRadioButtonId();
			assertTrue("check(value) sets  RadioButton", ((RadioButton) solo.getView(id) ).isChecked());
			rbIds[offset++] = id;
			g.check(id); // check using radioGroup		
			assertTrue("check(Enum) not consistent with check(int)", g.getCheckedValue() == ec);
			assertTrue("isSetToDefault() is incorrect", 
					(ec == g.getDefault()) == g.isSetToDefault() );
		}
		// check with ids
		offset = 0;
		int pid = 0;
		for(int id : rbIds) {
			g.check(id); // check using RadioGroupApi
			assertTrue("check(value) sets  RadioButton", ((RadioButton) solo.getView(id) ).isChecked());
			// check using radioGroup
			assertTrue( "check(int) sets wrong value", g.getCheckedValue() == values[offset++]); 	
			assertTrue("RadioButton ids are not contiguous", pid == 0 || pid == id - 1);
			pid = id;
		}
		
		assertTrue("RadioButton ids are not contiguous", 
				rbIds[rbIds.length - 1] - rbIds[0] == rbIds.length - 1);
		
		g.check(-1);
		assertTrue("check(-1) should set default", g.isSetToDefault());
		
	}
	
	private static class EGRDisector<T extends Enum<T>> {
		public final EnumRadioGroup<T> egr;
		public final int[] rbid;
		public final T[] values;
		public final RadioButton[] rb;
		
		private EGRDisector(EnumRadioGroup<T> egr) {
			this.egr = egr;
			values = egr.values();
			this.rbid = new int[values.length];
			this.rb = new RadioButton[values.length];
			int offset = 0;
			for( T ec : values) {
				
				rb[offset] = egr.findViewByEnum(ec);
				rbid[offset] = rb[offset].getId();
				++offset;
			}
		}
		
		public static <T extends Enum<T>> EGRDisector<T> make(EnumRadioGroup<T> egr) {
			return new EGRDisector<T>(egr);
		}
	}
	
	interface Functor<T> {
		void apply(T t );
	}
	
	private <T extends Enum<T>> void glissando( EnumRadioGroup<T> egr,  Functor<EGRDisector<T>> f) {
		f.apply(EGRDisector.make(egr));
	}
	
	
	
	
}
	
	
	/*	// Click on ladygaga
		solo.clickOnView(solo.getView(android.R.id.text1));

		assertTrue("feed_view not found", solo.waitForView(solo
				.getView(course.labs.fragmentslab.R.id.feed_view)));

		// Assert that: 'the audience cheering!' is shown
		assertTrue("'the audience cheering!' is not shown!",
				solo.searchText("the audience cheering!"));

		// Wait for onActivityCreated() Log Message:
		assertTrue("onActivityCreated() Log Message not found",
				solo.waitForLogMessage("Entered onActivityCreated()",timeout));

		// Wait for onItemSelected(0) Log Message:
		assertTrue("onItemSelected(0) Log Message not found",
				solo.waitForLogMessage("Entered onItemSelected(0)",timeout));

		// Wait for updateFeedDisplay() Log Message:
		assertTrue("updateFeedDisplay() Log Message not found",
				solo.waitForLogMessage("Entered updateFeedDisplay()",timeout));

		// Clear log
		solo.clearLog();
		
		// Press menu back key
		solo.goBack();
		
		// Wait for view: 'android.R.id.text1'
		assertTrue("text1 not found", solo.waitForView(android.R.id.text1));
		
		// Click on msrebeccablack
		solo.clickOnView(solo.getView(android.R.id.text1, 1));

		// Assert that: feed_view is shown
		assertTrue("feed_view! is not shown!", solo.waitForView(solo
				.getView(course.labs.fragmentslab.R.id.feed_view)));

		// Assert that: 'save me from school' is shown
		assertTrue("'save me from school' is not shown!",
				solo.searchText("save me from school"));

		// Wait for onActivityCreated() Log Message:
		assertTrue("onActivityCreated() Log Message not found",
				solo.waitForLogMessage("Entered onActivityCreated()",timeout));

		// Wait for Log Message:
		assertTrue("onItemSelected(1) Log Message not found",
				solo.waitForLogMessage("Entered onItemSelected(1)",timeout));

		// Wait for updateFeedDisplay() Log Message:
		assertTrue("updateFeedDisplay() Log Message not found",
				solo.waitForLogMessage("Entered updateFeedDisplay()",timeout));

		// Clear log
		solo.clearLog();

		// Press menu back key
		solo.goBack();

		// Click on taylorswift13
		solo.clickOnView(solo.getView(android.R.id.text1, 2));

		// Assert that: feed_view shown
		assertTrue("feed_view not shown", solo.waitForView(solo
				.getView(course.labs.fragmentslab.R.id.feed_view)));
		
		// Assert that: 'I love you guys so much' is shown
		assertTrue("'I love you guys so much' is not shown!",
				solo.searchText("I love you guys so much"));
		
		// Wait for onActivityCreated() Log Message:
		assertTrue("onActivityCreated() Log Message not found",
				solo.waitForLogMessage("Entered onActivityCreated()",timeout));

		// Wait for onItemSelected(2) Log Message:
		assertTrue("onItemSelected(2) Log Message not found",
				solo.waitForLogMessage("Entered onItemSelected(2)",timeout));

		// Wait for updateFeedDisplay() Log Message:
		assertTrue("updateFeedDisplay() Log Message not found",
				solo.waitForLogMessage("Entered updateFeedDisplay()",timeout));

	}*/


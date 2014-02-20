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
	
	public static final int LONG_ENOUGH_FOR_CALLBACKS_TO_RUN = 1000;
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
	
	
	public void testGlissando() {
		
		for( EnumRadioGroup<?> egr : egrs) {
			EGRDissector.make(egr).accept(new Scalpel() {
				
				@Override
				public void apply(EGRDissector<?> t) {
					for( int offset = 0 ; offset < t.rb.length; ++offset) {
						RadioButton rb = t.rb[offset];
						
						if( rb.getVisibility() != View.VISIBLE) {
							continue;
						}
						Enum<?> priorValue = t.erg.getCheckedValue();
						solo.clickOnView(rb);
						solo.sleep(LONG_ENOUGH_FOR_CALLBACKS_TO_RUN);

						Enum<?> expectedValue = t.values[offset];
						int expectedRbId = t.rbid[offset];
						if( expectedValue != priorValue) {
							// The callback is only triggered on a change!
							assertTrue( "Got wrong value from callback " + t.cbresult.currentValue + " " + expectedValue,
								t.cbresult.currentValue == expectedValue);
							assertTrue( "Got wrong id from callback", t.cbresult.checkedId == expectedRbId);
						}
						assertTrue( "Got wrong value after click" + t.erg.getCheckedValue() + " " + expectedValue, 
								t.erg.getCheckedValue() == expectedValue);
						assertTrue( "Got wrong id after click", t.erg.getCheckedRadioButtonId() == expectedRbId);						
					}
					
				}
			});
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
	
	private static class EGRDissector<T extends Enum<T>> {
		public final EnumRadioGroup<T> erg;
		public final int[] rbid;
		public final T[] values;
		public final RadioButton[] rb;
		
		private static class CallbackResult<T extends Enum<T>> {
			T currentValue;
			int checkedId;
		}
		
		CallbackResult<T> cbresult = new CallbackResult<T>();
		
		private EGRDissector(EnumRadioGroup<T> erg) {
			this.erg = erg;
			values = erg.values();
			this.rbid = new int[values.length];
			this.rb = new RadioButton[values.length];
			int offset = 0;
			for( T ec : values) {
				
				rb[offset] = erg.findViewByEnum(ec);
				rbid[offset] = rb[offset].getId();
				++offset;
			}
			erg.setOnCheckedChangeListener( true, new EnumRadioGroup.OnCheckChangedListener<T>() {

				@Override
				public void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId) {
					assertTrue("Callback returned wrong group!", group == EGRDissector.this.erg);
					cbresult.currentValue = currentValue;
					cbresult.checkedId = checkedId;
					
				}
			});
		}
		
		public void accept( Scalpel s ) {
			s.apply(this);
		}
		
		public static <T extends Enum<T>> EGRDissector<T> make(EnumRadioGroup<T> egr) {
			return new EGRDissector<T>(egr);
		}
	}
	
	interface Functor<T> {
		void apply(T t );
	}
	
	public abstract class Scalpel implements Functor<EGRDissector<?>> {
		
	}
	
	private <T extends Enum<T>> void glissando( EGRDissector<T> dissector,  Scalpel f) {
		f.apply(dissector);
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


package org.diffenbach.enumradiogroup.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.diffenbach.android.widgets.EnumRadioGroup;
import org.diffenbach.enumradiogroup.MainActivity;
import org.diffenbach.enumradiogroup.MainActivity.Agreed;
import org.diffenbach.enumradiogroup.MainActivity.Pet;
import org.diffenbach.enumradiogroup.MainActivity.Pie;
import org.diffenbach.enumradiogroup.MainActivity.Sex;
import org.diffenbach.enumradiogroup.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.robotium.solo.Solo;


public class EnumRadioGroupTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	public static final int LONG_ENOUGH_FOR_CALLBACKS_TO_RUN = 1000;
	private Solo solo;
	
	MainActivity mainActivity;
	EnumRadioGroup<?> ergs[] = new EnumRadioGroup<?>[5];
	EnumRadioGroup<Agreed> xmlAgreed;
	EnumRadioGroup<Sex> xmlSex;
	EnumRadioGroup<Agreed> pAgreed;
	EnumRadioGroup<Pie> pPies;
	EnumRadioGroup<Pet> pPets;
	Enum<?> defaults[] = {Agreed.NO, Sex.REQUIRED_FIELD, Agreed.NO, Pie.POTATO, Pet.NONE};
	Enum<?> values[][] = {Agreed.values(), Sex.values(), Agreed.values(), Pie.values(), Pet.values()};
	EnumSet<?> hidden[] = {	EnumSet.noneOf(Agreed.class), 
							EnumSet.of(Sex.REQUIRED_FIELD),
							EnumSet.of(Agreed.NO),
							EnumSet.of(Pie.APPLE),
							EnumSet.noneOf(Pet.class)};
	Class<?> enumClasses[] = { Agreed.class, Sex.class, Agreed.class, Pie.class, Pet.class}; 
	
	public EnumRadioGroupTest() {
		super(MainActivity.class);
	}

	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation());
		mainActivity = getActivity();
		
		// get  views:
		ergs[0] = xmlAgreed = (EnumRadioGroup<Agreed>) solo.getView(R.id.agreed1);
		ergs[1] = xmlSex = (EnumRadioGroup<Sex>) solo.getView(R.id.sex);
		ergs[2] = pAgreed = (EnumRadioGroup<Agreed>) solo.getView(MainActivity.P_AGREED_ID);
		ergs[3] = pPies = (EnumRadioGroup<Pie>) solo.getView(MainActivity.P_PIES_ID);
		ergs[4] = pPets = (EnumRadioGroup<Pet>) solo.getView(MainActivity.P_PETS_ID);
		
		
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	
	public void testViewsArePresent() {

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
	
	@UiThreadTest
	public void testDefaultsAreCorrectAndChecked() {
		int offset = 0 ;
		for( EnumRadioGroup<?> erg : ergs) {
			Enum<?> v = defaults[offset++];
			verifyDefaultValue(erg, v);
			verifyCheckedValue(erg, v);
			verifyCheckedValueIsChecked(erg);
		}
	}
		
	public void testAllEnumsHaveACorrspondingRadioButton() {
		for( EnumRadioGroup<?> erg : ergs) {
			verifyAllRadioButtonsExist(erg);
		}
	}
	
	public void testValuesArrayIsIdenticalToEnumValues() {
		int offset = 0;
		for( EnumRadioGroup<?> erg : ergs) {
			verifyValuesArrayIsCorrect(erg, values[offset++]);
		}
	}
	
	public void testRadioButtonFilteringWorks() {
		int offset = 0;
		for( EnumRadioGroup<?> erg : ergs) {
			verifyRadioButtonsHidden(erg, offset++);
		}
	}
		
	
	// warning: this test takes about a minute to run
	public void testGlissando() {		
		clickingSetsCorrectValue.apply(ergs);
	}
	
	@UiThreadTest
	public void testAPIsConsitant() {		
		new ScalpelGroup( 
				radioButtonIdsAreContiguous,
				ergAPIConsistantWithRadioGroupAPI, 
				radioGroupAPIConsistantWithErgAPI).apply(ergs);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> void verifyDefaultValue(EnumRadioGroup<T> g, Enum<?> val) {
		T v = (T) val;
		assertTrue("Default is not correct " + v, g.getDefault() == v);
		g.check(v);
		assertTrue("isDefault lies ",  g.isSetToDefault());
		T[] vals = g.values();
		g.check(vals[ (v.ordinal() + 1) % vals.length]);
		assertTrue("isDefault lies ",  vals.length == 1 || ! g.isSetToDefault());
		g.check(-1);
		assertTrue("isDefault lies ",  g.isSetToDefault());
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
			assertNotNull("Radio button does not exist", rb);
			assertSame("Radio button has wrong parent", g, rb.getParent());
			assertSame("Radio button doesn't match findViewByEnum", rb, g.findViewByEnum(ec));
		}
	}
	
	private void verifyValuesArrayIsCorrect(EnumRadioGroup<?> g, Enum<?>[] a ) {
		assertTrue("Values arrays are unequal ", Arrays.equals(g.values(), a));
	}
	
	private <T extends Enum<T>> void verifyRadioButtonVisibility(EnumRadioGroup<T> g, 
			EnumSet<T> gone, int visibility) {
		for( T ec: gone ) {
			assertEquals( "Wrong visibility ",  visibility, g.findViewByEnum(ec).getVisibility());
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> void verifyRadioButtonsHidden(EnumRadioGroup<T> g, int goneOffset) {
		EnumSet<T> gone = (EnumSet<T>) hidden[goneOffset];
		verifyRadioButtonVisibility(g, gone, View.GONE);
		verifyRadioButtonVisibility(g, EnumSet.complementOf(gone), View.VISIBLE);
	}
	
	
	//dead code
	private <T extends Enum<T>> void verifyContiguousAndConsistentWithRadioButtonsGroupAPI(EnumRadioGroup<T> g) {
		T[] values = g.values();
		int[] rbIds = new int[values.length];
		int offset = 0; 
		
		//check with values
		for(T ec : values) {
			int id;
			// raises CalledFromWrongThreadException! if @UITest not applied
			g.check(ec); // check using erg API
			id = g.getCheckedRadioButtonId();
			assertTrue("check(value) sets  RadioButton", ((RadioButton) solo.getView(id) ).isChecked());
			rbIds[offset++] = id;
			g.check(id); // check using radioGroup		
			assertSame("check(Enum) not consistent with check(int)", g.getCheckedValue(), ec);
			assertTrue("isSetToDefault() is incorrect", 
					(ec == g.getDefault()) == g.isSetToDefault() );
		}
		// check with ids
		offset = 0;
		int pid = 0;
		for(int id : rbIds) {
			g.check(id); // check using RadioGroupApi
			assertTrue("check(id) sets  RadioButton", ((RadioButton) solo.getView(id) ).isChecked());
			// check using radioGroup
			assertEquals( "check(int) sets wrong value", g.getCheckedValue(), values[offset++]); 	
			assertTrue("RadioButton ids are not contiguous", pid == 0 || pid == id - 1);
			pid = id;
		}
		
		assertTrue("RadioButton ids are not contiguous", 
				rbIds[rbIds.length - 1] - rbIds[0] == rbIds.length - 1);
		
	}
	
	/**
	 * Iterates over an EnumRadioGroup's values, 
	 * in order to collect its privates.
	 * @author TP DIffenbach
	 *
	 * @param <T>
	 */
	private static class ergDissector<T extends Enum<T>> {
		public final EnumRadioGroup<T> erg;
		public final int[] rbid;
		public final T[] values;
		public final RadioButton[] rb;
		
		private static class CallbackResult<T extends Enum<T>> {
			T currentValue;
			int checkedId;
		}
		
		CallbackResult<T> cbresult = new CallbackResult<T>();
		
		private ergDissector(EnumRadioGroup<T> erg) {
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
			erg.setOnCheckedChangeListener( new EnumRadioGroup.OnCheckedChangeListener<T>() {
			

				@Override
				public void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId) {
					assertTrue("Callback returned wrong group!", group == ergDissector.this.erg);
					cbresult.currentValue = currentValue;
					cbresult.checkedId = checkedId;
					
				}
			});
		}
		
		public ergDissector<T> apply( Scalpel s ) {
			s.apply(this);
			return this;
		}
		
		public static <T extends Enum<T>> ergDissector<T> make(EnumRadioGroup<T> erg) {
			return new ergDissector<T>(erg);
		}
	}
	
	/**
	 * Uses a dissector's data to apply tests to an EnumRadioGroup.
	 * @author TP DIffenbach
	 *
	 */
	public static abstract class Scalpel {
		abstract <T extends Enum<T>> void apply(ergDissector<T> erg);
		
		/**
		 * Make a disection kit for each EnumRadioGroup
		 * @param ergs
		 */
		void apply( EnumRadioGroup<?>... ergs) {
			for( EnumRadioGroup<?> erg: ergs) {
				apply(ergDissector.make(erg));
			}
		}
		
		/**
		 * Apply this scalpel ro each disection kit
		 * @param dis
		 */
		void apply( ergDissector<?>... dis) {
			for( ergDissector<?> d: dis) {
				apply(d);
			}
		}
	}
	
	public static class ScalpelGroup extends Scalpel {
		Scalpel[] group;
		
		public ScalpelGroup(Scalpel... group) {
			super();
			this.group = group;
		}


		@Override
		<T extends Enum<T>> void apply(ergDissector<T> erg) {
			for( Scalpel s : group) {
				s.apply(erg);
			}
			
		}
	}
	
	public static abstract class IteratingScapel extends Scalpel {
		/** Make several permutations and apply this Scalpel in that order
		 * 
		 */
		final <T extends Enum<T>> void apply(ergDissector<T> dissector) {
			
			Iterator<int[]> orders = permutations(dissector.values.length);
			while( orders.hasNext()) {
				int[] lookup = orders.next();
				Log.i("ORDERS", Arrays.toString(lookup));
			
				for(int i : lookup) {
					T before = dissector.erg.getCheckedValue();
					forAllValues(dissector.erg, dissector.values[i], dissector.rb[i], dissector.rbid[i]);
					verifyCallback(dissector, before);
					
				}
				
				T defval = dissector.erg.getDefault();
				int ordinal = defval.ordinal();
				T before = dissector.erg.getCheckedValue();
				forDefaultValue( dissector.erg, defval, dissector.rb[ordinal], dissector.rbid[ordinal]);
				verifyCallback(dissector, before);
			}
		}
		
		private <T extends Enum<T>>  void verifyCallback(ergDissector<T> dissector, T valueBefore ) {
			T after = dissector.erg.getCheckedValue();
			if( valueBefore != after) { //value did change
				assertEquals( "Got wrong value from callback", dissector.cbresult.currentValue, after);
				assertEquals( "Got wrong id from callback", 
						dissector.cbresult.checkedId, dissector.erg.getCheckedRadioButtonId());
			}
		}
		
		abstract <T extends Enum<T>> void forAllValues(EnumRadioGroup<T> erg, T value, RadioButton rb, int rbId);
		abstract <T extends Enum<T>> void forDefaultValue(EnumRadioGroup<T> erg, T value, RadioButton rb, int rbId);
	}
	
	public static abstract class IteratingScapelNoDefaultTest extends IteratingScapel {
		
		@Override
		final <T extends Enum<T>> void forDefaultValue(EnumRadioGroup<T> erg,
				T value, RadioButton rb, int rbId) {
			// NO-OP
			
		}
	}
	
	private final IteratingScapel clickingSetsCorrectValue = new IteratingScapelNoDefaultTest() {

		@Override
		<T extends Enum<T>> void forAllValues(EnumRadioGroup<T> erg, T value, RadioButton rb, int rbId) {

			if( rb.getVisibility() != View.VISIBLE) {
				return; // Robotium can't click hidden buttons
			}
			solo.clickOnView(rb);
			solo.sleep(LONG_ENOUGH_FOR_CALLBACKS_TO_RUN);
			assertEquals( "Got wrong value after click",  value, erg.getCheckedValue()); 
			assertEquals( "Got wrong id after click", rbId, erg.getCheckedRadioButtonId());
		}
	};
	
	private final IteratingScapel ergAPIConsistantWithRadioGroupAPI = new IteratingScapelNoDefaultTest() {
		
		@Override
		<T extends Enum<T>> void forAllValues(EnumRadioGroup<T> erg, T value, RadioButton rb, int rbId) {
			erg.check(value); // check using erg API
			assertEquals( "Didn't get expected rbId", rbId, erg.getCheckedRadioButtonId());
			assertTrue("check(id) sets RadioButton", ((RadioButton) solo.getView(rbId) ).isChecked());
			assertTrue("check(id) sets RadioButton", rb.isChecked());
			erg.check(rbId); // check using radioGroup		
			assertSame("check(Enum) not consistent with check(int)", value, erg.getCheckedValue());
			assertTrue("isSetToDefault() is incorrect", 
					(value == erg.getDefault()) == erg.isSetToDefault() );
			
		}
	};
	
	
	private final IteratingScapel radioGroupAPIConsistantWithErgAPI = new IteratingScapel() {

		@Override
		<T extends Enum<T>> void forAllValues(EnumRadioGroup<T> erg, T value, RadioButton rb, int rbId) {
			erg.check(rbId);
			assertSame("check(id) sets RadioButton", rb, solo.getView(rbId) );
			assertTrue("check(id) sets RadioButton", ((RadioButton) solo.getView(rbId) ).isChecked());
			assertTrue("check(id) sets RadioButton", rb.isChecked());
			assertEquals( "check(int) sets wrong value", erg.getCheckedValue(), value);
		}

		@Override
		<T extends Enum<T>> void forDefaultValue(EnumRadioGroup<T> erg,	T value, RadioButton rb, int rbId) {
			erg.check(-1);
			assertEquals( value, erg.getDefault());
			assertEquals( erg.getDefault(),erg.getCheckedValue());
			assertEquals( rbId, erg.getCheckedRadioButtonId());
		}
	};
	
	
	
	
	private final Scalpel radioButtonIdsAreContiguous = new Scalpel() {

		@Override
		<T extends Enum<T>> void apply(ergDissector<T> d) {
			// TODO Auto-generated method stub
			assertTrue("RadioButton ids are not contiguous", 
					d.rbid[d.rbid.length - 1] - d.rbid[0] == d.rbid.length - 1);
		}
		
	};
	
	/* private sttaics to make permuations*/
	
	
	private static void swap(int[] a, int i, int j) {
	    int tmp = a[i];
	    a[i] = a[j];
	    a[j] = tmp;
	}
	
	private static int[] permute(int[] a)
	{
	    for (int i =  a.length - 1; i > 0 ; --i) {
	        int j = (int) (Math.random() * i);
	        swap(a, i, j);
	    }
	    return a;
	}
	
	private static  Iterator<int[]> permutations( int[] source) {
		List<int[]> ret = new ArrayList<int[]>();
		ret.add(source);
		int[] copy = Arrays.copyOf(source, source.length);
		for( int i = 0; i < copy.length / 2; ++i ) {
			int j = copy.length - 1 - i;
			swap(copy, i, j);
		}
		ret.add(copy);
		ret.add(permute(Arrays.copyOf(source, source.length)));
		return ret.iterator();	
	}
	
	private static  Iterator<int[]> permutations( int n) {
		int[] source = new int[n];
		for( int i = 0 ; i < n ; ++i) {
			source[i] = i;
		}
		return permutations(source);
	}

}

package com.scouting.pathfinder;

import static com.scouting.pathfinder.CallLocation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Resolves a location callout or shorthand to its matching locations.
 * <p>
 * Input must already be trimmed and lowercased by the caller. A direction may be written before
 * or after the location name. Registration order is the final tie-breaker, so keys registered
 * earlier in the static block are treated as more common calls.
 * <p>
 * Spaced directional shorthands ("dray e", "north seer") are not registered; the parser derives
 * them from the {@code of(base, direction)} keys. Attached forms ("snemus") are registered because
 * they must also be fuzzy-matchable as ordinary directionless names.
 */
public class CallLocationFinder
{
	private static final Map<LocationKey, Set<CallLocation>> LOCATION_KEYS = new LinkedHashMap<>();
	private static final EnumSet<Direction> CARDINAL_DIRECTIONS = EnumSet.complementOf(EnumSet.of(Direction.NONE));
	private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();
	private static final Predicate<LocationKey> FUZZY_NONE = key -> key.direction == Direction.NONE;

	static
	{
		// Common locations
		key(ARCEUUS, of("arceuus"), of("arc"), of("botd5"));
		key(CHURCH, of("church"));
		key(DRAY, of("dray"), of("draynor"));
		key(DRAY_OAK, of("dray oak"), of("draynor oak"));
		key(EDGE, of("edge"), of("edgeville"));
		key(GLADE, of("glade"));
		key(MYTH, of("myth"));
		key(NEMUS, of("nemus"));
		key(NEMUS_S, of("snemus"), of("nemus", Direction.S));
		key(NSEER, of("nseer"), of("nseers"), of("seer", Direction.N), of("seers", Direction.N));
		key(NSORC, of("nsorc"), of("nmage"), of("sorc", Direction.N), of("mage", Direction.N));
		key(NWSEER, of("nwseer"), of("nwseers"), of("seer", Direction.NW), of("seers", Direction.NW));
		key(OASIS, of("oasis"), of("ajp", Direction.E));
		key(PMAGE, of("pmage"), of("prif mage"));
		key(PTEAK, of("pteak"), of("pmahog"), of("prif teak"), of("prif mahog"));
		key(SEER, of("seer"), of("seers"));
		key(SOULS_BJP_W, of("souls"), of("bjp", Direction.W));
		key(YAK, of("yak"), of("yaks"));
		key(ZALC, of("zalc"), of("zalcano"));

		// Uncommon locations
		key(AIS_E, of("ais", Direction.E));
		key(AIS_SW, of("ais", Direction.SW));
		key(APE, of("ape"), of("apeatoll"));
		key(BARB, of("barb"), of("ba"));
		key(BKP_W, of("bkp", Direction.W));
		key(CAMELOT, of("camelot"), of("cam"));
		key(CIR_W_MAHOG, of("cir w mahog"), of("cir", Direction.W));
		key(CIR_W_MAPLE, of("cir w maple"), of("cir", Direction.W));
		key(DRAY_EAST, of("dray", Direction.E), of("draynor", Direction.E));
		key(DRUMSTICK, of("drum"), of("ds"));
		key(ECABBAGE, of("ecabbage"), of("cabbage", Direction.E));
		key(FLAX, of("flax"));
		key(GE, of("ge"));
		key(GORGE, of("gorge"));
		key(HEART, of("heart"));
		key(HEART_E, of("kingstown"), of("heart", Direction.E));
		key(KASTORI, of("kastori"), of("kas"), of("kast"));
		key(LOOKOUT, of("lookout"));
		key(MIST_MAPLE_YEW, of("mist maple yew"), of("mist"), of("mistrock"));
		key(MTA_E, of("mta", Direction.E));
		key(MTA_W, of("mta", Direction.W));
		key(NCONCH, of("nconch"), of("cjq"), of("conch", Direction.N));
		key(NE_GLADE, of("neglade"), of("glade", Direction.NE));
		key(NEMUS_E, of("enemus"), of("nemus", Direction.E));
		key(NEMUS_SW, of("swnemus"), of("nemus", Direction.SW));
		key(NIEVE, of("nieve"));
		key(NWALD_MAGIC, of("nwald magic"), of("nwald"), of("ald magic", Direction.NW),
			of("ald", Direction.NW), of("ckq", Direction.NW));
		key(RADA_E, of("rada", Direction.E));
		key(RADA_SE, of("rada", Direction.SE));
		key(RADA_W, of("rada", Direction.W));
		key(RELLEKKA_SW, of("rpines"), of("rellekka", Direction.SW));
		key(RELEKKA_SW, of("rpines"), of("relekka", Direction.SW));
		key(SUNBLEAK, of("sb"), of("sunbleak"), of("sun"), of("sunb"));
		key(BOTD3_SHAY, of("botd3"), of("shay"), of("shayzien"));
		key(SORC, of("sorc"), of("smage"), of("mage", Direction.S));
		key(SW_CONCH, of("shore"), of("conch", Direction.SW), of("cjq", Direction.SW));
		key(TEKLAN, of("teklan"), of("tek"));
		key(VALE, of("vale"));
		key(VALE_E, of("vale", Direction.E), of("ais", Direction.N));
		key(VALE_S, of("vale", Direction.S));
		key(VALE_W, of("vale", Direction.W));

		// Obscure locations
		key(AJP, of("ajp"));
		key(APE_W, of("ape", Direction.W), of("apeatoll", Direction.W));
		key(ARDY_MONASTRY, of("ardy monastry"), of("ardy"));
		key(BATTLEFRONT, of("battlefront"));
		key(CATHERBY, of("catherby"), of("cath"));
		key(CHAMP, of("champ"), of("champion"));
		key(CHAMP_E, of("champ", Direction.E), of("champion", Direction.E));
		key(CORSAIR, of("corsair"));
		key(CRAFT, of("craft"));
		key(CW_EAST, of("cw", Direction.E));
		key(FALO, of("falo"));
		key(FALLY_GATE, of("fally gate"), of("fally"));
		key(FORTIS_POND_N, of("fortis"), of("fortis pond", Direction.N));
		key(FORTIS_POND_E, of("fortis"), of("fortis pond", Direction.E));
		key(GE_FOUNTAIN, of("ge water fountain"), of("ge fountain"));
		key(GROTTO, of("grotto"));
		key(GWENNITH, of("gwennith"));
		key(HSPIRIT, of("hspirit"));
		key(KASTORI_N, of("kastori", Direction.N), of("kas", Direction.N), of("kast", Direction.N));
		key(KASTORI_W, of("kastori", Direction.W), of("kas", Direction.W), of("kast", Direction.W));
		key(LGRAVE, of("lgrave"));
		key(LLETYA, of("lletya"));
		key(LUM_TREE_PATCH, of("lum tree patch"), of("ltree patch"), of("ltree"));
		key(MOS_LEHARM, of("mos leharm"));
		key(N_GLADE, of("nglade"), of("glade", Direction.N));
		key(N_TITHE_WILLOW, of("ntithe"), of("tithe willow", Direction.N), of("tithe", Direction.N));
		key(N_YAK, of("yak", Direction.N));
		key(NALD_YEW, of("nald yew"), of("nald"), of("naldarin"), of("ald yew", Direction.N),
			of("ald", Direction.N), of("aldarin", Direction.N));
		key(NCABBAGE, of("ncabbage"), of("cabbage", Direction.N));
		key(NEMUS_NW, of("nwnemus"), of("nemus", Direction.NW));
		key(PHAS, of("phas"));
		key(PISC, of("pisc"));
		key(RALOS_MAGICS, of("ralos magics"), of("ralos"));
		key(RIM, of("rim"), of("rimm"), of("rimmington"));
		key(RIMM_SE, of("rimm", Direction.SE));
		key(SARIM, of("sarim"));
		key(SCABBAGE, of("scabbage"), of("cabbage", Direction.S));
		key(SE_GNOME, of("gnome", Direction.SE));
		key(SE_PRIF, of("prif", Direction.SE));
		key(SHERLOCK, of("sherlock"));
		key(SINC, of("sinc"), of("sinclair"), of("sinclair mansion"));
		key(SINCLAIR, of("sinc"), of("sinclair"), of("sinclair mansion"));
		key(SLEGEND, of("slegend"), of("legend", Direction.S));
		key(SLEPE, of("slepe"));
		key(SLUMBER, of("slumber"), of("lumber", Direction.S));
		key(SW_FARM_GUILD, of("farm guild", Direction.SW), of("farm", Direction.SW));
		key(SW_HOS, of("hos", Direction.SW));
		key(SWLUMBER, of("swlumber"), of("lumber", Direction.SW));
		key(TBW_GROVE, of("tbw grove"), of("tbw"));
		key(TLATI_N, of("tlati", Direction.N));
		key(TLATI_NE, of("tlati", Direction.NE));
		key(UZER, of("uzer"));
		key(VALE_N, of("vale", Direction.N));
		key(VARROCK_W_BANK, of("varrock w bank"));
		key(WATSON, of("watson"));
		key(WC_TUTOR, of("wc tutor"));
		key(WCG, of("wcg"), of("wc guild"));
		key(WLEGEND, of("wlegend"), of("legend", Direction.W));
		key(WLUMBER, of("wlumber"), of("lumber", Direction.W));
		key(WOODLAND_NW, of("woodland", Direction.NW));
		key(ZANARIS, of("zanaris"));
	}

	private CallLocationFinder()
	{
	}

	/**
	 * Finds the locations represented by {@code input}.
	 * <p>
	 * The caller must trim and lowercase the input. Null, empty, direction-only and unrecognized
	 * input return an empty set. The returned set is always a fresh {@link LinkedHashSet}.
	 *
	 * @param input location name or shorthand
	 * @return matching locations in registration order, or an empty set
	 */
	public static Set<CallLocation> find(String input)
	{
		if (input == null || input.isEmpty() || isDirectionOnly(input))
		{
			return new LinkedHashSet<>();
		}
		return new LinkedHashSet<>(lookup(input));
	}

	// Lookup strategies: each is an ordered list of steps; the first non-empty result wins.
	private static Set<CallLocation> lookup(String input)
	{
		Set<LocationKey> spaced = parseDirections(input, true);
		if (!spaced.isEmpty())
		{
			return lookupSpaced(input, spaced);
		}

		Set<LocationKey> adjacent = parseDirections(input, false);
		if (!adjacent.isEmpty())
		{
			return lookupAdjacent(input, adjacent);
		}

		return firstMatch(
			() -> exactNone(input),
			() -> exactDirectionalBase(input),
			() -> fuzzyNone(input),
			() -> fuzzyDirectionalBase(input));
	}

	private static Set<CallLocation> lookupSpaced(String input, Set<LocationKey> parses)
	{
		return firstMatch(
			() -> earliest(parses),
			() -> exactNone(input),
			() -> fuzzyDirectional(parses),
			() -> fuzzyNone(input),
			() -> directionlessBase(parses));
	}

	/**
	 * Adjacent abbreviations ("ndray", "nwseer") are ambiguous with ordinary names, so complete-input
	 * matches are tried before any parsed interpretation.
	 */
	private static Set<CallLocation> lookupAdjacent(String input, Set<LocationKey> parses)
	{
		return firstMatch(
			() -> exactNone(input),
			() -> earliest(parses),
			() -> exactDirectionalBase(input),
			() -> fuzzyNone(input),
			() -> fuzzyDirectionalBase(input),
			() -> fuzzyDirectional(parses),
			() -> directionlessBase(parses));
	}

	@SafeVarargs
	private static Set<CallLocation> firstMatch(Supplier<Set<CallLocation>>... steps)
	{
		for (Supplier<Set<CallLocation>> step : steps)
		{
			Set<CallLocation> match = step.get();
			if (!match.isEmpty())
			{
				return match;
			}
		}
		return Collections.emptySet();
	}

	/** An explicit direction may fall back to a directionless key for its base, never to another direction. */
	private static Set<CallLocation> directionlessBase(Set<LocationKey> parses)
	{
		Set<LocationKey> bases = new LinkedHashSet<>();
		for (LocationKey parse : parses)
		{
			bases.add(of(parse.base));
		}
		Set<CallLocation> exact = earliest(bases);
		if (!exact.isEmpty())
		{
			return exact;
		}

		FuzzyCandidate best = null;
		for (LocationKey parse : parses)
		{
			best = better(best, bestFuzzy(parse.base, FUZZY_NONE));
		}
		return resolve(best);
	}

	private static boolean isDirectionOnly(String input)
	{
		return CARDINAL_DIRECTIONS.stream()
			.anyMatch(direction -> input.equals(direction.abbreviation) || input.equals(direction.fullName));
	}

	/**
	 * Returns every way {@code input} can be read as a base with a direction at the start or end.
	 * Spaced parses (direction separated by whitespace) and adjacent parses (abbreviation attached
	 * to the base) are requested separately. Full names are only recognised when separated by
	 * whitespace, and an abbreviation is never split off the front of a full name ("northdray"
	 * is not "n" + "orthdray").
	 */
	private static Set<LocationKey> parseDirections(String input, boolean spaced)
	{
		boolean startsWithName = CARDINAL_DIRECTIONS.stream().anyMatch(d -> input.startsWith(d.fullName));
		Set<LocationKey> parses = new LinkedHashSet<>();

		for (Direction direction : CARDINAL_DIRECTIONS)
		{
			String[] tokens = spaced
				? new String[] {direction.abbreviation, direction.fullName}
				: new String[] {direction.abbreviation};

			for (String token : tokens)
			{
				int rest = input.length() - token.length();
				if (rest <= 0)
				{
					continue;
				}

				// "<direction> <base>" or "<abbreviation><base>"
				if (input.startsWith(token)
					&& Character.isWhitespace(input.charAt(token.length())) == spaced
					&& (spaced || !startsWithName))
				{
					addParse(parses, input.substring(token.length()).stripLeading(), direction);
				}

				// "<base> <direction>" or "<base><abbreviation>"
				if (input.endsWith(token) && Character.isWhitespace(input.charAt(rest - 1)) == spaced)
				{
					addParse(parses, input.substring(0, rest).stripTrailing(), direction);
				}
			}
		}
		return parses;
	}

	private static void addParse(Set<LocationKey> parses, String base, Direction direction)
	{
		if (!base.isEmpty())
		{
			parses.add(new LocationKey(base, direction));
		}
	}

	private static Set<CallLocation> exactNone(String input)
	{
		return LOCATION_KEYS.getOrDefault(of(input), Collections.emptySet());
	}

	/** The earliest registered key among {@code wanted} wins; candidates are never combined. */
	private static Set<CallLocation> earliest(Collection<LocationKey> wanted)
	{
		for (Map.Entry<LocationKey, Set<CallLocation>> entry : LOCATION_KEYS.entrySet())
		{
			if (wanted.contains(entry.getKey()))
			{
				return entry.getValue();
			}
		}
		return Collections.emptySet();
	}

	/** All directional variants registered for a base, in registration order. */
	private static Set<CallLocation> exactDirectionalBase(String base)
	{
		Set<CallLocation> variants = new LinkedHashSet<>();
		for (Map.Entry<LocationKey, Set<CallLocation>> entry : LOCATION_KEYS.entrySet())
		{
			if (entry.getKey().direction != Direction.NONE && entry.getKey().base.equals(base))
			{
				variants.addAll(entry.getValue());
			}
		}
		return variants;
	}

	private static Set<CallLocation> fuzzyNone(String input)
	{
		return resolve(bestFuzzy(input, FUZZY_NONE));
	}

	private static Set<CallLocation> fuzzyDirectional(Set<LocationKey> parses)
	{
		FuzzyCandidate best = null;
		for (LocationKey parse : parses)
		{
			best = better(best, bestFuzzy(parse.base, key -> key.direction == parse.direction));
		}
		return resolve(best);
	}

	/** Matches a base, then returns every directional variant of it. */
	private static Set<CallLocation> fuzzyDirectionalBase(String input)
	{
		FuzzyCandidate best = bestFuzzy(input, key -> key.direction != Direction.NONE);
		return best == null ? Collections.emptySet() : exactDirectionalBase(best.key.base);
	}

	private static Set<CallLocation> resolve(FuzzyCandidate candidate)
	{
		return candidate == null ? Collections.emptySet() : LOCATION_KEYS.get(candidate.key);
	}

	private static FuzzyCandidate bestFuzzy(String input, Predicate<LocationKey> eligible)
	{
		String collapsed = collapseRepeats(input);
		FuzzyCandidate best = null;
		int order = 0;
		for (LocationKey key : LOCATION_KEYS.keySet())
		{
			order++;
			FuzzyCandidate candidate = evaluateFuzzyCandidate(input, collapsed, key, order);
			if (candidate != null && eligible.test(key))
			{
				best = better(best, candidate);
			}
		}
		return best;
	}

	/** Returns null when the key is not an acceptable fuzzy match for the input. */
	private static FuzzyCandidate evaluateFuzzyCandidate(String input, String collapsedInput, LocationKey key, int order)
	{
		String candidate = key.base;
		if (input.length() < 4 || candidate.length() < 4
			|| input.charAt(0) != candidate.charAt(0))
		{
			return null;
		}

		boolean anagram = isAnagram(input, candidate);
		int distance = Math.min(LEVENSHTEIN.apply(input, candidate), LEVENSHTEIN.apply(collapsedInput, candidate));
		int threshold = Math.min(3, candidate.length() / 3);
		if (!anagram && distance > threshold)
		{
			return null;
		}
		return new FuzzyCandidate(key, distance, anagram, order);
	}

	/** Anagrams first, then lowest distance, then earliest registration. */
	private static FuzzyCandidate better(FuzzyCandidate current, FuzzyCandidate challenger)
	{
		if (challenger == null)
		{
			return current;
		}
		if (current == null)
		{
			return challenger;
		}
		if (challenger.anagram != current.anagram)
		{
			return challenger.anagram ? challenger : current;
		}
		if (challenger.distance != current.distance)
		{
			return challenger.distance < current.distance ? challenger : current;
		}
		return challenger.order < current.order ? challenger : current;
	}

	private static boolean isAnagram(String a, String b)
	{
		return a.length() == b.length() && Arrays.equals(sorted(a), sorted(b));
	}

	private static char[] sorted(String text)
	{
		char[] chars = text.toCharArray();
		Arrays.sort(chars);
		return chars;
	}

	/** Collapses runs of the same character: "drrayy" becomes "dray". */
	private static String collapseRepeats(String input)
	{
		StringBuilder collapsed = new StringBuilder();
		for (int i = 0; i < input.length(); i++)
		{
			if (i == 0 || input.charAt(i) != input.charAt(i - 1))
			{
				collapsed.append(input.charAt(i));
			}
		}
		return collapsed.toString();
	}

	private static LocationKey of(String base)
	{
		return new LocationKey(base, Direction.NONE);
	}

	private static LocationKey of(String base, Direction direction)
	{
		return new LocationKey(base, direction);
	}

	private static void key(CallLocation location, LocationKey... keys)
	{
		for (LocationKey locationKey : keys)
		{
			LOCATION_KEYS.computeIfAbsent(locationKey, k -> new LinkedHashSet<>()).add(location);
		}
	}

	private enum Direction
	{
		NONE("", ""),
		N("n", "north"),
		NE("ne", "northeast"),
		E("e", "east"),
		SE("se", "southeast"),
		S("s", "south"),
		SW("sw", "southwest"),
		W("w", "west"),
		NW("nw", "northwest");

		private final String abbreviation;
		private final String fullName;

		Direction(String abbreviation, String fullName)
		{
			this.abbreviation = abbreviation;
			this.fullName = fullName;
		}
	}

	private static final class FuzzyCandidate
	{
		private final LocationKey key;
		private final int distance;
		private final boolean anagram;
		private final int order;

		private FuzzyCandidate(LocationKey key, int distance, boolean anagram, int order)
		{
			this.key = key;
			this.distance = distance;
			this.anagram = anagram;
			this.order = order;
		}
	}

	/** A lowercase base plus a direction; also used for the parsed readings of an input. */
	private static final class LocationKey
	{
		private final String base;
		private final Direction direction;

		private LocationKey(String base, Direction direction)
		{
			this.base = base;
			this.direction = direction;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof LocationKey))
			{
				return false;
			}
			LocationKey other = (LocationKey) obj;
			return base.equals(other.base) && direction == other.direction;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(base, direction);
		}
	}
}
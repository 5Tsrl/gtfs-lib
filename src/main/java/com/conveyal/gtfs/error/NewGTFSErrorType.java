package com.conveyal.gtfs.error;

import com.conveyal.gtfs.validator.model.Priority;

public enum NewGTFSErrorType {

DATE_FORMAT(Priority.MEDIUM, "Il formato data deve essere YYYYMMDD."),
DATE_RANGE(Priority.MEDIUM, "La data Ã¨ molto lontana nel futuro o nel passato."),
DATE_NO_SERVICE(Priority.MEDIUM, "Non vi sono service_id attivi per una data compresa nell'intervallo di date con un servizio definito."),
TIME_FORMAT(Priority.MEDIUM, "Il tempo seve essere definito secondo il formato HH:MM:SS."),
URL_FORMAT(Priority.MEDIUM, "Il formato della URL dovrebbe essere <scheme>://<authority><path>?<query>#<fragment>"),
LANGUAGE_FORMAT(Priority.LOW, "Il linguaggio deve essere specificato con un tag BCP47 valido."),
INTEGER_FORMAT(Priority.MEDIUM, "Formato intero non corretto."),
FLOATING_FORMAT(Priority.MEDIUM, "Formato numero floating point non corretto."),
COLUMN_NAME_UNSAFE(Priority.HIGH, "L'intestazione della colonna contiene caratteri non sicuri in SQL, Ã¨ stato rinominato."),
NUMBER_PARSING(Priority.MEDIUM, "Non Ã¨ stato possibile parsificare il numero dal valore."),
NUMBER_NEGATIVE(Priority.MEDIUM, "Il numero atteso non dovrebbe essere negativo."),
NUMBER_TOO_SMALL(Priority.MEDIUM, "Il numero Ã¨ inferiore al range ammissibile."),
NUMBER_TOO_LARGE(Priority.MEDIUM, "Il numero Ã¨ superiore al range ammissibile."),
DUPLICATE_ID(Priority.MEDIUM, "PiÃ¹ di una entitÃ  nella tabella ha lo stesso ID."),
DUPLICATE_TRIP(Priority.MEDIUM, "PiÃ¹ di una corsa ha lo stesso orario e le stesse fermate."),
DUPLICATE_STOP(Priority.MEDIUM, "PiÃ¹ di una fermata Ã¨ posizionata nella stessa posizione."),
DUPLICATE_HEADER(Priority.MEDIUM, "PiÃ¹ di una colonna nella tabella ha lo stesso nome nella riga di intestazione."),
MISSING_TABLE(Priority.MEDIUM, "Questa tabella Ã¨ richiesta dalla specifica GTFS ma non Ã¨ presente."),
MISSING_COLUMN(Priority.MEDIUM, "Una colonna obbligatoria non Ã¨ presente nella tabella."),
MISSING_SHAPE(Priority.MEDIUM, "???"),
MISSING_FIELD(Priority.MEDIUM, "Manca un campo richiesto o Ã¨ vuoto per una specifica riga."),
MULTIPLE_SHAPES_FOR_PATTERN(Priority.MEDIUM, "PiÃ¹ percorsi trovati per una singola sequenza di fermate (es. trip pattern)."),
WRONG_NUMBER_OF_FIELDS(Priority.MEDIUM, "Una riga non ha lo stesso numero di campi rispetto all'intestazione della tabella."),
NO_SERVICE(Priority.HIGH, "Non vi sono servizi definiti per alcun giorno in questo feed."),
OVERLAPPING_TRIP(Priority.MEDIUM, "Blocchi?"),
SHAPE_REVERSED(Priority.MEDIUM, "Un percorso sembra sia associato a veicoli che percorrono la direzione opposta della linea."),
SHAPE_MISSING_COORDINATE(Priority.MEDIUM, "???"),
TABLE_IN_SUBDIRECTORY(Priority.HIGH, "Invece di essere nella radice del file zip, una tabella Ã¨ stata innestata in una sottocartella."),
TABLE_MISSING_COLUMN_HEADERS(Priority.HIGH, "Nella tabella manca l'intestazione delle colonne."),
TABLE_TOO_LONG(Priority.MEDIUM, "La tabella Ã¨ troppo lunga per registrare numero di linea interi a 32-bit integer, ci sarÃ  un problema di overflow."),
TIME_ZONE_FORMAT(Priority.MEDIUM, "Il formato del time zone dovrebbe essere X."),
REQUIRED_TABLE_EMPTY(Priority.MEDIUM, "Questa tabella Ã¨ richiesta dalla specifica GTFS ma Ã¨ vuota."),
ROUTE_DESCRIPTION_SAME_AS_NAME(Priority.LOW, "La descrizione di una linea Ã¨ identica al suo nome , quindi non aggiunge alcuna informazione."),
ROUTE_LONG_NAME_CONTAINS_SHORT_NAME(Priority.LOW, "Il long name di una linea dovrebbe essere complementare allo short name, non includerlo."),
ROUTE_SHORT_AND_LONG_NAME_MISSING(Priority.MEDIUM, "Una linea non ha nÃ¨ long nÃ¨ short name."),
ROUTE_SHORT_NAME_TOO_LONG(Priority.MEDIUM, "Lo short name di una linea Ã¨ troppo lungo per essere mostrato nella comuni applicazioni che usano il GTFS."),
SERVICE_NEVER_ACTIVE(Priority.MEDIUM, "Un service code Ã¨ stato definito, ma non Ã¨ stato utilizzato in alcuna data."),
SERVICE_UNUSED(Priority.MEDIUM, "Un service code Ã¨ stato definito, ma non Ã¨ mai stato indicato da alcuna corsa."),
SHAPE_DIST_TRAVELED_NOT_INCREASING(Priority.MEDIUM, "La shape distance traveled deve essere incrementale con gli orari dei passaggi in fermata."),
STOP_LOW_POPULATION_DENSITY(Priority.HIGH, "Una fermata Ã¨ posizionata in un'area geografica con una bassa densitÃ  di popolazione."),
STOP_GEOGRAPHIC_OUTLIER(Priority.HIGH, "Questa fermata Ã¨ posizionata molto distante dal 90% delle fermate di questo feed."),
STOP_UNUSED(Priority.MEDIUM, "Questa fermata non Ã¨ utilizzata da alcuna corsa."),
TRIP_EMPTY(Priority.HIGH, "Questa corsa Ã¨ definita ma non ha orari di passaggio in fermata."),
TRIP_NEVER_ACTIVE(Priority.MEDIUM, "Una corsa Ã¨ definita, ma non Ã¨ operativa in alcuna data."),
ROUTE_UNUSED(Priority.HIGH, "Questa linea Ã¨ definita ma non ha corse."),
TRAVEL_DISTANCE_ZERO(Priority.MEDIUM, "Il veicolo non percorre alcuna distanza tra l'ultima fermata e questa."),
TRAVEL_TIME_NEGATIVE(Priority.HIGH, "Il veicolo arriva a questa fermata prima di partire dalla precedente."),
TRAVEL_TIME_ZERO(Priority.HIGH, "Il veicolo arriva a questa fermata allo stesso tempo in cui parte dalla precedente."),
MISSING_ARRIVAL_OR_DEPARTURE(Priority.MEDIUM, "E' richiesto che il primo e l'ultimo passaggio in fermata abbiano sia l'orario di arrivo che di partenza."),
TRIP_TOO_FEW_STOP_TIMES(Priority.MEDIUM, "Una corsa deve avere almeno 2 passaggi in fermata per rappresentare il tragitto."),
TRIP_OVERLAP_IN_BLOCK(Priority.MEDIUM, "Blocchi"),
TRAVEL_TOO_SLOW(Priority.MEDIUM, "Il veicolo sta viaggiando molto lentamente per raggiungere questa fermata rispetto a quella precedente."),
TRAVEL_TOO_FAST(Priority.MEDIUM, "Il veicolo sta viaggiando molto velocemente per raggiungere questa fermata rispetto a quella precedente.."),
VALIDATOR_FAILED(Priority.HIGH, "Lo specifico processo di validazione Ã¨ fallito per un errore occorso durante il caricamento. Questo sembrerebbe un errore incontrato nel caricamento (es. una data o un campo numerico non Ã¨ formattato correttamente.)."),
DEPARTURE_BEFORE_ARRIVAL(Priority.MEDIUM, "Il veicolo parte da questa fermata prima che arrivi."),
REFERENTIAL_INTEGRITY(Priority.HIGH, "Questa linea fa riferimento ad un ID che non esiste nella tabella di riferimento."),
BOOLEAN_FORMAT(Priority.MEDIUM, "Un campo booleano del GTFS deve contenere il valore 1 o 0."),
COLOR_FORMAT(Priority.MEDIUM, "Un colore deve essere specificato con una notazione in 6 caratteri (3 numeri esadecimali di 2 cifre)."),
CURRENCY_UNKNOWN(Priority.MEDIUM, "Il codice della valuta non Ã¨ stato riconosciuto."),
OTHER(Priority.LOW, "Altri errori.");

    public final Priority priority;
    public final String englishMessage;

    NewGTFSErrorType(Priority priority, String englishMessage) {
        this.priority = priority;
        this.englishMessage = englishMessage;
    }

}

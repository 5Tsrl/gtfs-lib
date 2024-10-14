package com.conveyal.gtfs.error;

import com.conveyal.gtfs.validator.model.Priority;

/**
 * This enum defines the GTFS error types that can be encountered when validating GTFS table data. Each error type has a
 * severity level and related error message.
 */
public enum NewGTFSErrorType {
    // Standard errors.
    AGENCY_ID_REQUIRED_FOR_MULTI_AGENCY_FEEDS(Priority.HIGH, "Per i feed GTFS che hanno più di una agency, è obbligatorio specificare il campo agency_id per ogni linea nel file routes.txt."),
    BOOLEAN_FORMAT(Priority.MEDIUM, "un campo booleano può contenere solo valori 1 oppure 0."),
    COLOR_FORMAT(Priority.MEDIUM, "Il colore deve essere specificato con la notazione a 6 caratteri (3 numeri esadecimali di 2 cifre)."),
    COLUMN_NAME_UNSAFE(Priority.HIGH, "L'intestazione della colonna contiene caratteri non sicuri in SQL, è stato rinominata."),
    CONDITIONALLY_REQUIRED(Priority.HIGH, "Manca un campo in certe condizioni obbligatorio."),
    CURRENCY_UNKNOWN(Priority.MEDIUM, "Codice valuta non valido."),
    DATE_FORMAT(Priority.MEDIUM, "Il formato data deve essere YYYYMMDD."),
    DATE_NO_SERVICE(Priority.MEDIUM, "Non vi sono service_id attivi per una data compresa nell'intervallo di date con un servizio definito."),
    DATE_RANGE(Priority.MEDIUM, "La data è molto lontana nel futuro o nel passato."),
    DEPARTURE_BEFORE_ARRIVAL(Priority.MEDIUM, "Il veicolo parte da questa fermata prima che arrivi."),
    DUPLICATE_HEADER(Priority.MEDIUM, "Più di una colonna nella tabella ha lo stesso nome nella riga di intestazione."),
    DUPLICATE_ID(Priority.MEDIUM, "Più di una entità nella tabella ha lo stesso ID."),
    DUPLICATE_STOP(Priority.MEDIUM, "Più di una fermata è localizzata nella stessa posizione."),
    DUPLICATE_TRIP(Priority.MEDIUM, "Più di una corsa ha lo stesso orario e le stesse fermate."),
    DUPLICATEDCALENDARS(Priority.MEDIUM, "Alcuni calendari risultano avere la stessa definizione di giorni di servizio. Sarebbe opportuno evitare la proliferazione dei calendari."),
    DUPLICATEDCODCORSA(Priority.MEDIUM, "Alcuni percorsi risultano avere corse con codice corsa duplicato. È necessario che il codice corsa sia univoco all'interno di un percorso."),
    DUPLICATEDCODCORSAINROUTE(Priority.MEDIUM, "Alcune linee risultano avere corse con codice corsa duplicato. È necessario che il codice corsa sia univoco all'interno di una linea."),
    FARE_TRANSFER_MISMATCH(Priority.MEDIUM, "Una tariffa che non consente i trasferimenti ha una durata di trasferimento diversa da zero."),
    FEED_TRAVEL_TIMES_ROUNDED(Priority.LOW, "Tutti i tempi di viaggio del feed sono arrotondati al minuto; ciò potrebbe provocare risultati anomali nelle applicazioni di calcolo percorso, con tempi di viaggio pari a 0."),
    FLOATING_FORMAT(Priority.MEDIUM, "Formato numero floating point non corretto."),
    FREQUENCY_PERIOD_OVERLAP(Priority.MEDIUM, "La frequenza di una corsa si sovrappone con un'altra frequenza definita per la stessa corsa."),
    ILLEGAL_FIELD_VALUE(Priority.MEDIUM, "Il campo non deve contenere tab o carattere <a capo>."),
    INTEGER_FORMAT(Priority.MEDIUM, "Formato intero non corretto."),
    LANGUAGE_FORMAT(Priority.LOW, "La lingua deve essere specificata con un codice BCP47 valido."),
    MISSING_ARRIVAL_OR_DEPARTURE(Priority.MEDIUM, "È richiesto che il primo e l'ultimo passaggio in fermata abbiano sia l'orario di arrivo che di partenza."),
    MISSING_COLUMN(Priority.MEDIUM, "Una colonna obbligatoria non è presente nella tabella."),
    MISSING_FIELD(Priority.MEDIUM, "Manca un campo richiesto o è vuoto in una specifica riga."),
    MISSING_FOREIGN_TABLE_REFERENCE(Priority.HIGH, "Questo oggetto si riferisce ad un ID che deve esistere in una sola tabella di riferimento."),
    MISSING_SHAPE(Priority.MEDIUM, "Manca la geometria di un percorso"),
    MISSING_TABLE(Priority.MEDIUM, "Questa tabella è richiesta dalla specifica GTFS ma non è presente."),
    MULTIPLE_SHAPES_FOR_PATTERN(Priority.MEDIUM, "Più percorsi trovati per una singola sequenza di fermate (es. trip pattern)."),
    NO_SERVICE(Priority.HIGH, "Non vi sono servizi definiti per alcun giorno in questo feed."),
    NUMBER_NEGATIVE(Priority.MEDIUM, "Il numero atteso non dovrebbe essere negativo."),
    NUMBER_PARSING(Priority.MEDIUM, "Non è stato possibile parsificare il numero dal valore."),
    NUMBER_TOO_LARGE(Priority.MEDIUM, "Il numero è superiore al massimo ammissibile."),
    NUMBER_TOO_SMALL(Priority.MEDIUM, "Il numero è inferiore al minimo ammissibile."),
    OVERLAPPING_TRIP(Priority.MEDIUM, "Presenza di corse sovrapposte."),
    REFERENTIAL_INTEGRITY(Priority.HIGH, "Questa linea fa riferimento ad un ID che non esiste nella tabella di riferimento."),
    REQUIRED_TABLE_EMPTY(Priority.MEDIUM, "Questa tabella è richiesta dalla specifica GTFS ma è vuota."),
    ROUTE_DESCRIPTION_SAME_AS_NAME(Priority.LOW, "La descrizione di una linea è identica al suo nome , quindi non aggiunge alcuna informazione."),
    ROUTE_LONG_NAME_CONTAINS_SHORT_NAME(Priority.LOW, "Il nome lungo della linea (route long name) dovrebbe essere complementare allo nome breve (route short name), non includerlo."),
    ROUTE_SHORT_AND_LONG_NAME_MISSING(Priority.MEDIUM, "Una linea non ha nè nome lungo (route long name) nè nome breve (route short name)."),
    ROUTE_SHORT_NAME_TOO_LONG(Priority.MEDIUM, "Il nome breve (route short name) di una linea è troppo lungo per essere mostrato nelle più comuni applicazioni che usano il GTFS."),
    ROUTE_TYPE_INVALID(Priority.MEDIUM, "Tipo linea non valido."),
    ROUTE_UNUSED(Priority.HIGH, "Questa linea è definita ma non ha corse."),
    SERVICE_NEVER_ACTIVE(Priority.MEDIUM, "Il service code non è attivo in alcuna data."),
    SERVICE_UNUSED(Priority.MEDIUM, "Il service code non è utilizzato da alcuna corsa."),
    SERVICE_WITHOUT_DAYS_OF_WEEK(Priority.MEDIUM, "Il Calendario non definisce alcun giorno della settimana come validità di base."),
    SHAPE_DIST_TRAVELED_NOT_INCREASING(Priority.MEDIUM, "La shape distance traveled deve essere incrementale con gli orari dei passaggi in fermata."),
    SHAPE_MISSING_COORDINATE(Priority.MEDIUM, "La geometria ha alcune coordinate mancanti"),
    SHAPE_REVERSED(Priority.MEDIUM, "Un percorso sembra sia associato a veicoli che percorrono la direzione opposta della linea."),
    SPURIOUSTRIPSINPATTERN(Priority.MEDIUM, "Alcuni percorsi risultano avere al loro interno delle corse con km contrattuali diversi da quelli riportarti nel percorso. Valutare se spostare tali corse in nuovi percorsi."),
    STOP_DESCRIPTION_SAME_AS_NAME(Priority.LOW, "La descrizione della fermata è identica al suo nome e non aggiunge alcuna informazione."),
    STOP_GEOGRAPHIC_OUTLIER(Priority.HIGH, "Questa fermata è posizionata molto distante dal 90% delle fermate di questo feed."),
    STOP_LOW_POPULATION_DENSITY(Priority.HIGH, "Una fermata è posizionata in un'area geografica con una bassa densità di popolazione."),
    STOP_NAME_MISSING(Priority.MEDIUM, "La fermata non ha nome."),
    STOP_TIME_UNUSED(Priority.LOW, "Questo orario non permette nè la salita nè la discesa e non è un timepoint, quindi non è utile e deve essere rimosso dalla corsa."),
    STOP_UNUSED(Priority.MEDIUM, "Questa fermata non è utilizzata da alcuna corsa."),
    TABLE_IN_SUBDIRECTORY(Priority.HIGH, "Invece di essere nella radice del file zip, una tabella è stata innestata in una sottocartella."),
    TABLE_MISSING_COLUMN_HEADERS(Priority.HIGH, "Nella tabella manca l'intestazione delle colonne."),
    TABLE_TOO_LONG(Priority.MEDIUM, "La tabella è troppo lunga per registrare numeri di linea interi a 32-bit integer, ci sarà un problema di overflow."),
    TIME_FORMAT(Priority.MEDIUM, "Il tempo seve essere definito secondo il formato HH:MM:SS."),
    TIME_ZONE_FORMAT(Priority.MEDIUM, "Il formato del fuso orario dovrebbe corrispondere ad uno dei valori elencati nel Time Zone Database https://en.wikipedia.org/wiki/List_of_tz_database_time_zones."),
    TIMEPOINT_MISSING_TIMES(Priority.MEDIUM, "Questo orario è definito come timepoint, ma mancano sia l'orario di partenza che di arrivo."),
    TRAVEL_DISTANCE_ZERO(Priority.MEDIUM, "Il veicolo non percorre alcuna distanza tra l'ultima fermata e questa."),
    TRAVEL_TIME_NEGATIVE(Priority.HIGH, "Il veicolo arriva a questa fermata prima di partire dalla precedente."),
    TRAVEL_TIME_ZERO(Priority.HIGH, "Il veicolo arriva a questa fermata allo stesso orario in cui parte dalla precedente."),
    TRAVEL_TOO_FAST(Priority.MEDIUM, "Il veicolo sta viaggiando molto velocemente per raggiungere questa fermata rispetto a quella precedente."),
    TRAVEL_TOO_SLOW(Priority.MEDIUM, "Il veicolo sta viaggiando molto lentamente per raggiungere questa fermata rispetto a quella precedente."),
    TRIP_EMPTY(Priority.HIGH, "Questa corsa è definita ma non ha orari di passaggio in fermata."),
    TRIP_HEADSIGN_CONTAINS_ROUTE_NAME(Priority.LOW, "Un trip headsign contiene il nome della linea, ma dovrebbe contenere solo informazioni necessarie a distinguerlo dalle altre corse della linea."),
    TRIP_HEADSIGN_SHOULD_DESCRIBE_DESTINATION_OR_WAYPOINTS(Priority.LOW, "Un trip headsign inizia con 'A' o 'Verso', ma dovrebbe cominciare con la destinazione o la direzione e eventualmente includere punti di passaggio indicando 'via'"),
    TRIP_NEVER_ACTIVE(Priority.MEDIUM, "Una corsa è definita, ma non è operativa in alcuna data."),
    TRIP_OVERLAP_IN_BLOCK(Priority.MEDIUM, "Due corse con lo stesso block_id si sovrappongono nel tempo."),
    TRIP_TOO_FEW_STOP_TIMES(Priority.MEDIUM, "Una corsa deve avere almeno 2 passaggi in fermata per rappresentare il tragitto."),
    URL_FORMAT(Priority.MEDIUM, "Il formato della URL dovrebbe essere <scheme>://<authority><path>?<query>#<fragment>"),
    VALIDATOR_FAILED(Priority.HIGH, "Lo specifico processo di validazione è fallito per un errore occorso durante il caricamento. Questo sembrerebbe un errore incontrato nel caricamento (es. una data o un campo numerico non è formattato correttamente)."),
    WRONG_NUMBER_OF_FIELDS(Priority.MEDIUM, "Riga senza lo stesso numero di campi rispetto all'intestazione della tabella."),

    // MTC-specific errors.
    FIELD_VALUE_TOO_LONG(Priority.MEDIUM, "Il campo ha troppi caratteri."),

    // Shared Stops-specifc errors.
    MULTIPLE_SHARED_STOPS_GROUPS(Priority.HIGH, "Una fermata appartiene a più di un gruppo di fermate."),
    SHARED_STOP_GROUP_MULTIPLE_PRIMARY_STOPS(Priority.HIGH, "Un gruppo di fermate ha più di una fermata principale."),
    SHARED_STOP_GROUP_ENTITY_DOES_NOT_EXIST(Priority.MEDIUM, "Un gruppo di fermate fa riferimento ad una fermata inesistente."),

    // Unknown errors.
    OTHER(Priority.LOW, "Altri errori.");


    public final Priority priority;
    public final String englishMessage;

    NewGTFSErrorType(Priority priority, String englishMessage) {
        this.priority = priority;
        this.englishMessage = englishMessage;
    }

}



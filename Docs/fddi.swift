import SwiftData

// Define the model structs that conform to FetchableRecord and PersistableRecord protocols
struct Program: FetchableRecord, PersistableRecord {
    var id: String?
    var name: String?
    var programs: [Program]?
    var projects: [Project]?
    var progress: Progress?
    var extensions: [String: Any]?

    static let databaseTableName = "Programs"
}

struct Project: FetchableRecord, PersistableRecord {
    var id: String?
    var name: String
    var aspects: [Aspect]?
    var progress: Progress?
    var extensions: [String: Any]?

    static let databaseTableName = "Projects"
}

struct Aspect: FetchableRecord, PersistableRecord {
    var id: String?
    var name: String
    var info: AspectInfo?
    var subjects: [Subject]?
    var progress: Progress?
    var extensions: [String: Any]?

    static let databaseTableName = "Aspects"
}

// Define other structs for AspectInfo, Subject, Activity, Feature, Milestone, Note, Progress, and KPI

// Encode data to JSON
let program = Program(name: "Program A", programs: [...], projects: [...])
let jsonEncoder = JSONEncoder()
if let jsonData = try? jsonEncoder.encode(program) {
    if let jsonString = String(data: jsonData, encoding: .utf8) {
        print(jsonString)
    }
}

// Decode JSON data
let jsonDecoder = JSONDecoder()
if let jsonData = jsonString.data(using: .utf8) {
    if let decodedProgram = try? jsonDecoder.decode(Program.self, from: jsonData) {
        print(decodedProgram)
    }
}

// Persist data using SwiftData
let database = Database(path: "/path/to/database.sqlite")
try database.registerTable(Program.self)
try database.registerTable(Project.self)
try database.registerTable(Aspect.self)
// Register other tables

let program = Program(name: "Program A", programs: [...], projects: [...])
try database.persist(program)

// Fetch data from database
let programs = try database.fetchAll(Program.self)

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 * Journal
 *
 * Tachyon's Write Ahead Log.  All mutations to master's state must get logged here.
 *
 * Journal class itself is a container for the real log, which can be found in
 * {@link tachyon.master.EditLog}.  Each mutation creates a {@link tachyon.master.EditLogOperation}
 * which gets serialized to {@link tachyon.UnderFileSystem} in the form of JSON messages.
 *
 * When the server starts up, this log will be read one line at a time; recreating each message
 * and replying each event into {@link tachyon.master.MasterInfo}.
 *
 * Image
 *
 * The {@link tachyon.master.EditLog} is used to keep track of each mutation, but tachyon also has
 * another file that stores the state of the server: {@link tachyon.master.Image}.
 *
 * {@link tachyon.master.Image} is effectively a serialized copy of the master's state at a given
 * point in time.  Again, this file is written to {@link tachyon.UnderFileSystem} just like
 * the {@link tachyon.master.EditLog}, and like the log, it is also a JSON list of messages where
 * each message is either: version, {@link tachyon.master.Dependency} for lineage information,
 * {@link tachyon.master.InodeFolder} which contains the file system tree, checkpoint, and/or
 * {@link tachyon.master.RawTables}.
 *
 * When the server starts up, this file will be read and the state stored in it will be used as
 * the master's current state.
 *
 *
 * {@link tachyon.master.Journal}
 *
 * {@link tachyon.master.MasterInfo}
 *
 * Zookeeper {@link tachyon.LeaderSelectorClient}
 *
 * Services
 *
 * Thrift
 *
 * Delegates to {@link tachyon.master.MasterInfo}
 *
 * Web
 *
 * {@link tachyon.web.UIWebServer}
 *
 */
package tachyon.master;
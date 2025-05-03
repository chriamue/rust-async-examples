# Glossary

## Arc
Short for "Atomic Reference Counted." A thread-safe reference-counting pointer that enables shared ownership of data across threads or tasks.

## Async
A programming model that allows for non-blocking, concurrent execution of tasks. In Rust, async code is often used with runtimes like Tokio.

## Async/Await
A syntax in Rust (and other languages) for writing asynchronous code that looks and behaves similarly to synchronous code, making it easier to read and maintain. `async` defines an asynchronous function, and `.await` pauses execution until a future is ready.

## Await
The operation that pauses the execution of an async function until a future is ready to yield a value. In Rust, this is done with the `.await` keyword.

## Blocking
An operation that prevents the current thread from doing other work until it completes. Blocking should be minimized in async code to avoid stalling the executor.

## Channel
A mechanism for sending data or messages between tasks. Tokio provides several types of channels for asynchronous communication and synchronization.

## Executor
A component responsible for polling and running asynchronous tasks (futures) to completion. In Tokio, the runtime acts as the executor.

## Futures
Objects representing values that may not be available yet. In Rust async programming, a future is a computation that can be polled and will eventually yield a result.

## Handle
A reference to a running task or to the runtime itself. For example, a `JoinHandle` allows you to await the result of a spawned task, and a `RuntimeHandle` lets you interact with the Tokio runtime from outside its context.

## HTTP
A protocol for transmitting data over the internet, commonly used for web browsing, APIs, and file transfer.

## JoinHandle
A handle returned by `tokio::spawn` that can be awaited to retrieve the result of a spawned task.

## Mutex
A mutual exclusion primitive useful for protecting shared data. In async Rust, `tokio::sync::Mutex` is commonly used to allow safe, concurrent access to data across tasks.

## Non-blocking
Operations that allow the program to continue executing other tasks while waiting for an operation to complete. This is a key feature of async programming.

## Pin
A Rust concept used in async programming to guarantee that the memory location of a future does not change, which is required for certain types of self-referential data.

## Reactor
A component in async runtimes (including Tokio) that waits for I/O events and wakes up tasks when they are ready to make progress. Sometimes called the event loop.

## Runtime
The core component in Tokio that manages the execution of asynchronous tasks. It provides the necessary infrastructure for scheduling, polling, and running tasks.

## Scheduler
A component within the runtime that determines the order and timing in which tasks are executed. Tokio uses work-stealing schedulers to efficiently distribute tasks across threads.

## Send/Sync
Rust traits that determine whether types can be safely transferred (`Send`) or shared (`Sync`) between threads. Many async runtimes require futures to be `Send` so they can be executed on thread pools.

## Spawn
The act of starting a new asynchronous task on the runtime. In Tokio, this is done with `tokio::spawn`.

## Task
A unit of asynchronous work that can be executed concurrently. In Tokio, tasks are created by spawning futures onto the runtime.

## Thread
A lightweight process that can run concurrently within a single program. Tokio uses a thread pool to execute tasks in parallel.

## Tokio
A popular asynchronous runtime for Rust, providing tools and abstractions for building fast, concurrent, and reliable network applications.

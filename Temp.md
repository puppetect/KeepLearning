Spring Batch

## Definition
Spring batch is the processing of a finite amount of data wihout interaction or iterruption



## JsonPath Syntax
Expression | Description
--- | ---
`$` | The root object or array.
`.property` | Selects the specified property in a parent object.
`['property']` | Selects the specified property in a parent object. Be sure to put single quotes around the property name. **Tip:** Use this notation if the property name contains special characters such as spaces, or begins with a character other than A..Za..z_.
`[n]` | Selects the n-th element from an array. Indexes are 0-based.
`[index1,index2,…]` | Selects array elements with the specified indexes. Returns a list.
`..property` | Recursive descent: Searches for the specified property name recursively and returns an array of all values with this property name. Always returns a list, even if just one property is found.
`*` | Wildcard selects all elements in an object or an array, regardless of their names or indexes. For example, address.* means all properties of the address object, and book[*] means all items of the book array.
`[start:end]` `[start:]` | Selects array elements from the start index and up to, but not including, end index. If end is omitted, selects all elements from start until the end of the array. Returns a list.
`[:n]` | Selects the first n elements of the array. Returns a list.
`[-n:]` | Selects the last n elements of the array. Returns a list.
`[?(expression)]` | Filter expression. Selects all elements in an object or array that match the specified filter. Returns a list.
`[(expression)]` | Script expressions can be used instead of explicit property names or indexes. An example is [(@.length-1)] which selects the last item in an array. Here, length refers to the length of the current array rather than a JSON field named length.
`@` | Used in filter expressions to refer to the current node being processed.

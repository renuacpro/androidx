/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.compiler.plugins.kotlin.lower

import androidx.compose.compiler.plugins.kotlin.hasComposableAnnotation
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.descriptors.IrBasedDeclarationDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.symbols.impl.*
import org.jetbrains.kotlin.ir.util.DeepCopySymbolRemapper
import org.jetbrains.kotlin.ir.util.DescriptorsRemapper
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.types.KotlinType

/**
 * This symbol remapper is aware of possible descriptor signature change to align
 * function signature and descriptor signature in cases of composable value parameters.
 * It removes descriptors whenever the signature changes, forcing it to be generated from IR.
 *
 * For example, when function has a signature of:
 * ```
 * fun A(@Composable f: () -> Unit)
 * ```
 * it is going to be converted to incompatible signature of:
 * ```
 * fun A(f: (Composer<*>, Int) -> Unit)
 * ```
 * Same applies for receiver and return types.
 *
 * This conversion is only required with decoys, but can be applied to the JVM as well for
 * consistency.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
class ComposableSymbolRemapper : DeepCopySymbolRemapper(
    object : DescriptorsRemapper {
        override fun remapDeclaredConstructor(
            descriptor: ClassConstructorDescriptor
        ): ClassConstructorDescriptor? =
            descriptor.takeUnless { it.isTransformed() }

        override fun remapDeclaredSimpleFunction(
            descriptor: FunctionDescriptor
        ): FunctionDescriptor? =
            descriptor.takeUnless { it.isTransformed() }

        override fun remapDeclaredValueParameter(
            descriptor: ParameterDescriptor
        ): ParameterDescriptor? =
            descriptor.takeUnless { it.isTransformed() }

        override fun remapDeclaredTypeParameter(
            descriptor: TypeParameterDescriptor
        ): TypeParameterDescriptor? =
            descriptor.takeUnless { it.isTransformed() }

        private fun ClassConstructorDescriptor.isTransformed(): Boolean =
            this is IrBasedDeclarationDescriptor<*> ||
                valueParameters.any { it.type.containsComposable() }

        private fun FunctionDescriptor.isTransformed(): Boolean =
            this is IrBasedDeclarationDescriptor<*> ||
                valueParameters.any { it.type.containsComposable() } ||
                returnType?.containsComposable() == true

        private fun ParameterDescriptor.isTransformed(): Boolean =
            this is IrBasedDeclarationDescriptor<*> ||
                type.containsComposable() ||
                containingDeclaration.let { it is FunctionDescriptor && it.isTransformed() }

        private fun TypeParameterDescriptor.isTransformed(): Boolean =
            this is IrBasedDeclarationDescriptor<*> ||
                containingDeclaration.let { it is FunctionDescriptor && it.isTransformed() }

        private fun KotlinType.containsComposable() =
            hasComposableAnnotation() ||
                arguments.any { it.type.hasComposableAnnotation() }
    }
) {
    // TODO: the DeepCopySymbolRemapper in Kotlin compiler loses symbol signatures.
    // Consider using DeepCopySymbolRemapperPreservingSignatures after moving to 1.5.20.
    override fun visitClass(declaration: IrClass) {
        remapSymbol(classes, declaration) {
            if (declaration.symbol is IrClassPublicSymbolImpl) {
                IrClassPublicSymbolImpl(declaration.symbol.signature!!)
            } else {
                IrClassSymbolImpl(descriptorsRemapper.remapDeclaredClass(it.descriptor))
            }
        }
        declaration.acceptChildrenVoid(this)
    }
}